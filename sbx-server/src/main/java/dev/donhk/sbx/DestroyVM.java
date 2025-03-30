package dev.donhk.sbx;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.ActiveMachineRow;
import dev.donhk.pojos.HostPortStatus;
import dev.donhk.pojos.Rule;
import dev.donhk.vbox.VBoxManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DestroyVM {

    private final Logger logger = LoggerFactory.getLogger(DestroyVM.class);
    private final VMDataAccessService VMDataAccessService;
    private final Map<String, Integer> ports;
    private final boolean hasNatNetwork;
    private final String natNetwork;
    private final String vmName;
    private final String lastState;
    private final VBoxManager vBoxManager;

    private DestroyVM(VMDataAccessService VMDataAccessService,
                      Map<String, Integer> ports,
                      boolean hasNatNetwork,
                      String natNetwork,
                      String vmName,
                      String lastState, VBoxManager vBoxManager) {
        this.VMDataAccessService = VMDataAccessService;
        this.ports = ports;
        this.hasNatNetwork = hasNatNetwork;
        this.natNetwork = natNetwork;
        this.vmName = vmName;
        this.lastState = lastState;
        this.vBoxManager = vBoxManager;
    }

    static DestroyVM newInstance(VMDataAccessService VMDataAccessService,
                                 Map<String, Integer> ports,
                                 boolean hasNatNetwork,
                                 String natNetwork, String vmName,
                                 String lastState,
                                 VBoxManager vBoxManager) {
        return new DestroyVM(VMDataAccessService, ports, hasNatNetwork, natNetwork, vmName, lastState, vBoxManager);
    }

    void destroy() {
        //disassemble the network
        disassembleNetwork();
        //destroy the vm from the host
        destroyVM();
    }

    private void destroyVM() {
        logger.info("Cleaning up machine " + vmName);
        //destroy vm
        try {
            vBoxManager.cleanUpVM(vmName);
        } catch (Exception e) {
            logger.warn("Error removing machine " + e.getMessage(), e);
        }
        logger.info("Waiting confirmation of machine removal for " + vmName);
        try {
            while (vBoxManager.machineExists(vmName)) {
                Thread.sleep(100L);
            }
            logger.info("The machine " + vmName + " was successfully removed");
        } catch (InterruptedException e) {
            logger.warn(vmName + " " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warn("error detecting vm, I will continue with clean up");
        }
        removeVMMetadataInDB();
    }

    private void disassembleNetwork() {
        logger.info("Removing network configurations for " + vmName);
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
                logger.info("The nat network " + natNetwork + " cannot be dropped because it is being used by other VMs");
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
        logger.info("Removing rules associated with vm " + vmName + " from nat network " + natNetwork);
        try {
            final List<Rule> rules = VMDataAccessService.getRules(vmName);
            for (Rule rule : rules) {
                vBoxManager.rmNATNetworkPortForwardRule(natNetwork, rule.rule_name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        markPortsAsFreeInDB();
    }

    private void removeRulesOfThisVM() {
        try {
            final List<Rule> rules = VMDataAccessService.getRules(vmName);
            for (Rule rule : rules) {
                vBoxManager.rmNATPortForwardRule(vmName, rule.rule_name);
            }
        } catch (Exception e) {
            logger.warn("error removing rule: " + e.getMessage());
        }
        markPortsAsFreeInDB();
    }

    private void markPortsAsFreeInDB() {
        //remove any entry on rules table
        //a machine creation might have failed but the rule might have been added
        logger.info("Freeing ports used by " + vmName);
        try {
            VMDataAccessService.dropRule(vmName);
            for (Map.Entry<String, Integer> e : ports.entrySet()) {
                int port = e.getValue();
                String portName = e.getKey();
                logger.info("Freeing " + portName + " " + port + " " + vmName);
                VMDataAccessService.updatePort(port, HostPortStatus.FREE);
            }
        } catch (SQLException e) {
            logger.warn("error removing rules info " + e.getMessage(), e);
        }
    }

    private void removeVMMetadataInDB() {
        if (VMDataAccessService.machineExists(vmName)) {
            try {
                logger.info("Removing metadata of " + vmName);
                VMDataAccessService.updateVmState(vmName, lastState);
                VMDataAccessService.removeMachine(vmName);
            } catch (SQLException e) {
                logger.warn("error removing vm meta " + e.getMessage(), e);
            }
        }
    }

    private boolean isNatNetworkUsedByOtherVM() {
        try {
            for (ActiveMachineRow otherVM : VMDataAccessService.getActiveMachines()) {
                //ignore itself
                if (otherVM.name.equals(vmName)) {
                    continue;
                }
                if (otherVM.network.equals(natNetwork)) {
                    logger.info(otherVM.name + " is attached to " + natNetwork + " it cannot be dropped yet");
                    return true;
                }
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    private void removeWholeNatNetwork() {
        try {
            logger.info("Removing nat network " + vmName + " because no one else is using it");
            vBoxManager.removeNatNetwork(vmName);
        } catch (Exception e) {
            logger.warn("Error removing nat network " + e.getMessage(), e);
        }

        try {
            VMDataAccessService.dropNATNetwork(vmName);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
        }

    }
}
