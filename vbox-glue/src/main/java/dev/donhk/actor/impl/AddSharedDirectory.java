package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class AddSharedDirectory {

    private final VBoxManager boxManager;
    private final VBoxMessage.AddSharedDirectoryRequest request;

    public AddSharedDirectory(VBoxManager boxManager, VBoxMessage.AddSharedDirectoryRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.AddSharedDirectoryResponse dispatch() {
        Logger.info("AddSharedDirectoryRequest name={} dir={} host={}", request.machineName(), request.dirName(), request.hostPath());
        boolean success = boxManager.addSharedDirectory(request.machineName(), request.dirName(), request.hostPath());
        return new VBoxMessage.AddSharedDirectoryResponse(success);
    }
}
