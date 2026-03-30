package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class AddSharedStorageToMachine {

    private final VBoxManager boxManager;
    private final VBoxMessage.AddSharedStorageToMachineRequest request;

    public AddSharedStorageToMachine(VBoxManager boxManager, VBoxMessage.AddSharedStorageToMachineRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.AddSharedStorageToMachineResponse dispatch() {
        Logger.info("AddSharedStorageToMachineRequest name={} disks={}",
                request.machineName(), request.sharedStorageDisks().size());
        boolean success = boxManager.addSharedStorageToMachine(request.machineName(), request.sharedStorageDisks());
        return new VBoxMessage.AddSharedStorageToMachineResponse(success);
    }
}
