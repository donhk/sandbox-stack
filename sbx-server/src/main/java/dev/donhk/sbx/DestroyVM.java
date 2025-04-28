package dev.donhk.sbx;

import dev.donhk.database.DBService;
import dev.donhk.pojos.ActiveMachineRow;
import dev.donhk.pojos.HostPortStatus;
import dev.donhk.pojos.Rule;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DestroyVM {

    private final DBService DBService;
    private final Map<String, Integer> ports;
    private final boolean hasNatNetwork;
    private final String natNetwork;
    private final String vmName;
    private final String lastState;
    private final VBoxManager vBoxManager;

    private DestroyVM(DBService DBService,
                      Map<String, Integer> ports,
                      boolean hasNatNetwork,
                      String natNetwork,
                      String vmName,
                      String lastState, VBoxManager vBoxManager) {
        this.DBService = DBService;
        this.ports = ports;
        this.hasNatNetwork = hasNatNetwork;
        this.natNetwork = natNetwork;
        this.vmName = vmName;
        this.lastState = lastState;
        this.vBoxManager = vBoxManager;
    }

    static DestroyVM newInstance(DBService DBService,
                                 Map<String, Integer> ports,
                                 boolean hasNatNetwork,
                                 String natNetwork, String vmName,
                                 String lastState,
                                 VBoxManager vBoxManager) {
        return new DestroyVM(DBService, ports, hasNatNetwork, natNetwork, vmName, lastState, vBoxManager);
    }

    void destroy() {
        //disassemble the network
        disassembleNetwork();
        //destroy the vm from the host
        destroyVM();
    }

    private void destroyVM() {
        Logger.info("Cleaning up machine {}", vmName);
        //destroy vm
        try {
            vBoxManager.cleanUpVM(vmName);
        } catch (Exception e) {
            Logger.warn("Error removing machine {}", e.getMessage(), e);
        }
        Logger.info("Waiting confirmation of machine removal for {}", vmName);
        try {
            while (vBoxManager.machineExists(vmName)) {
                Thread.sleep(100L);
            }
            Logger.info("The machine {} was successfully removed", vmName);
        } catch (InterruptedException e) {
            Logger.warn("{} {}", vmName, e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Logger.warn("error detecting vm, I will continue with clean up");
        }
        removeVMMetadataInDB();
    }

    private void disassembleNetwork() {
        Logger.info("Removing network configurations for {}", vmName);
        if (!hasNatNetwork) {
            //no nat networks do not require any action from our side
            removeRulesOfThisVM();
            return;
        }
        //is this VM the owner of the nat network?
        if (vmName.equals(natNetwork)) {
            //yes, it is
            //is it being used by someone else?
            if (isNatNetworkUsedByOtherVM()) {
                Logger.info("The nat network {} cannot be dropped because it is being used by other VMs", natNetwork);
                //yes, just remove the rules associated with this machine
                removeRulesAssociatedWithThisNatNetwork();
            } else {
                //no, then drop the whole network
                removeWholeNatNetwork();
            }
        } else {
            //no, it is juts attached to it
            //just remove the rules associated with this machine
            removeRulesAssociatedWithThisNatNetwork();
        }
    }

    private void removeRulesAssociatedWithThisNatNetwork() {
        Logger.info("Removing rules associated with vm {} from nat network {}", vmName, natNetwork);
        try {
            final List<Rule> rules = DBService.getRules(vmName);
            for (Rule rule : rules) {
                vBoxManager.rmNATNetworkPortForwardRule(natNetwork, rule.rule_name);
            }
        } catch (SQLException e) {
            Logger.error("sql error", e);
        }
        markPortsAsFreeInDB();
    }

    private void removeRulesOfThisVM() {
        try {
            final List<Rule> rules = DBService.getRules(vmName);
            for (Rule rule : rules) {
                vBoxManager.rmNATPortForwardRule(vmName, rule.rule_name);
            }
        } catch (Exception e) {
            Logger.warn("error removing rule: {}", e.getMessage());
        }
        markPortsAsFreeInDB();
    }

    private void markPortsAsFreeInDB() {
        //remove any entry on rules table
        //a machine creation might have failed but the rule might have been added
        Logger.info("Freeing ports used by {}", vmName);
        try {
            DBService.dropRule(vmName);
            for (Map.Entry<String, Integer> e : ports.entrySet()) {
                int port = e.getValue();
                String portName = e.getKey();
                Logger.info("Freeing {} {} {}", portName, port, vmName);
                DBService.updatePort(port, HostPortStatus.FREE);
            }
        } catch (SQLException e) {
            Logger.warn("error removing rules info {}", e.getMessage(), e);
        }
    }

    private void removeVMMetadataInDB() {
        if (DBService.machineExists(vmName)) {
            try {
                Logger.info("Removing metadata of {}", vmName);
                DBService.updateVmState(vmName, lastState);
                DBService.removeMachine(vmName);
            } catch (SQLException e) {
                Logger.warn("error removing vm meta {}", e.getMessage(), e);
            }
        }
    }

    private boolean isNatNetworkUsedByOtherVM() {
        try {
            for (ActiveMachineRow otherVM : DBService.getActiveMachines()) {
                //ignore itself
                if (otherVM.name.equals(vmName)) {
                    continue;
                }
                if (otherVM.network.equals(natNetwork)) {
                    Logger.info("{} is attached to {} it cannot be dropped yet", otherVM.name, natNetwork);
                    return true;
                }
            }
        } catch (SQLException e) {
            Logger.error("sql err", e);
            return false;
        }
        return false;
    }

    private void removeWholeNatNetwork() {
        try {
            Logger.info("Removing nat network {} because no one else is using it", vmName);
            vBoxManager.removeNatNetwork(vmName);
        } catch (Exception e) {
            Logger.warn("Error removing nat network {}", e.getMessage(), e);
        }

        try {
            DBService.dropNATNetwork(vmName);
        } catch (SQLException e) {
            Logger.warn(e.getMessage(), e);
        }

    }
}
