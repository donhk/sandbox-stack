package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class CreateNatNetwork {

    private final VBoxManager boxManager;
    private final VBoxMessage.CreateNatNetworkRequest request;

    public CreateNatNetwork(VBoxManager boxManager, VBoxMessage.CreateNatNetworkRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.CreateNatNetworkResponse dispatch() {
        Logger.info("CreateNatNetworkRequest name={}", request.networkName());
        boolean success = boxManager.createNatNetwork(request.networkName());
        return new VBoxMessage.CreateNatNetworkResponse(success);
    }
}
