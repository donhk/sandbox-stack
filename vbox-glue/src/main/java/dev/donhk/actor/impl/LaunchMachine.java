package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class LaunchMachine {

    private final VBoxManager boxManager;
    private final VBoxMessage.LaunchMachineRequest request;

    public LaunchMachine(VBoxManager boxManager, VBoxMessage.LaunchMachineRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.LaunchMachineResponse dispatch() {
        Logger.info("LaunchMachineRequest name={} mode={}", request.machineName(), request.mode());
        boolean success = boxManager.launchMachine(request.machineName(), request.mode());
        return new VBoxMessage.LaunchMachineResponse(success);
    }
}
