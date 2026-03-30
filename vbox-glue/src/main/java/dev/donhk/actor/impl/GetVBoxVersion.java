package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class GetVBoxVersion {

    private final VBoxManager boxManager;

    public GetVBoxVersion(VBoxManager boxManager) {
        this.boxManager = boxManager;
    }

    public VBoxMessage.GetVBoxVersionResponse dispatch() {
        Logger.info("GetVBoxVersionRequest");
        return new VBoxMessage.GetVBoxVersionResponse(boxManager.getVBoxVersion());
    }
}
