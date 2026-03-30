package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class MachineExists {

    private final VBoxManager boxManager;
    private final VBoxMessage.MachineExistsRequest request;

    public MachineExists(VBoxManager boxManager, VBoxMessage.MachineExistsRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.MachineExistsResponse dispatch() {
        Logger.info("MachineExistsRequest name={}", request.machineName());
        boolean exists = boxManager.machineExists(request.machineName());
        return new VBoxMessage.MachineExistsResponse(exists);
    }
}
