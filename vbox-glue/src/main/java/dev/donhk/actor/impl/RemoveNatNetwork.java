package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class RemoveNatNetwork {

    private final VBoxManager boxManager;
    private final VBoxMessage.RemoveNatNetworkRequest request;

    public RemoveNatNetwork(VBoxManager boxManager, VBoxMessage.RemoveNatNetworkRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.RemoveNatNetworkResponse dispatch() {
        Logger.info("RemoveNatNetworkRequest name={}", request.networkName());
        boolean success = boxManager.removeNatNetwork(request.networkName());
        return new VBoxMessage.RemoveNatNetworkResponse(success);
    }
}
