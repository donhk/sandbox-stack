package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.pojos.ActiveMachineRow;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;
import org.virtualbox_7_1.INATNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DelDanglingNets {

    private final VBoxManager boxManager;
    private final Pattern natShape = Pattern.compile("\\w+_\\w+");
    private final List<ActiveMachineRow> machines;

    public DelDanglingNets(VBoxManager boxManager, List<ActiveMachineRow> machines) {
        this.boxManager = boxManager;
        this.machines = machines;
    }

    public VBoxMessage.DelDanglingNetsResponse dispatch() {

        final List<INATNetwork> networks = boxManager.getNatNetworks();
        final List<String> unusedNetworks = new ArrayList<>();

        for (INATNetwork network : networks) {
            final String netName = network.getNetworkName();
            final Matcher matcher = natShape.matcher(netName);
            //does it look like a network that we created?
            if (!matcher.find()) {
                //no, skip it
                continue;
            }

            //yes, then check if it is in use
            if (checkIfNetworkIsFree(netName, this.machines)) {
                unusedNetworks.add(netName);
                //this network is not longer in use
                Logger.info("Physical removing dangling network {}", netName);
                this.boxManager.removeNatNetwork(netName);
            }
        }
        return new VBoxMessage.DelDanglingNetsResponse(unusedNetworks);
    }

    private boolean checkIfNetworkIsFree(String netName, List<ActiveMachineRow> machines) {
        for (ActiveMachineRow activeVM : machines) {
            if (activeVM.network.equals(netName)) {
                //the network is still in use, thus we cannot drop it
                Logger.info("{} cannot be dropped because it is un use by {}", netName, activeVM.name);
                return false;
            }
        }
        return true;
    }
}
