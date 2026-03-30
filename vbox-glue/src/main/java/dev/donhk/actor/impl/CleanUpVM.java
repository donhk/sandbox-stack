package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class CleanUpVM {

    private final VBoxManager boxManager;
    private final VBoxMessage.CleanUpVMRequest request;

    public CleanUpVM(VBoxManager boxManager, VBoxMessage.CleanUpVMRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.CleanUpVMResponse dispatch() {
        Logger.info("CleanUpVMRequest name={}", request.machineName());
        boxManager.cleanUpVM(request.machineName());
        return new VBoxMessage.CleanUpVMResponse();
    }
}
