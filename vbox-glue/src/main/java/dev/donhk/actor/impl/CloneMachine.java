package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class CloneMachine {

    private final VBoxManager boxManager;
    private final VBoxMessage.CloneMachineRequest request;

    public CloneMachine(VBoxManager boxManager, VBoxMessage.CloneMachineRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.CloneMachineResponse dispatch() {
        Logger.info("CloneMachineRequest seed={} snapshot={} name={} net={}",
                request.seedName(), request.snapshot(), request.machineName(), request.natNetwork());
        boolean success = boxManager.cloneMachineFromSeed(
                request.seedName(), request.snapshot(), request.machineName(), request.natNetwork());
        return new VBoxMessage.CloneMachineResponse(success);
    }
}
