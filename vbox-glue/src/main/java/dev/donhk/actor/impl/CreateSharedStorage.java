package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

import java.util.List;

public class CreateSharedStorage {

    private final VBoxManager boxManager;
    private final VBoxMessage.CreateSharedStorageRequest request;

    public CreateSharedStorage(VBoxManager boxManager, VBoxMessage.CreateSharedStorageRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.CreateSharedStorageResponse dispatch() {
        Logger.info("CreateSharedStorageRequest name={} numDisks={} size={}GB",
                request.machineName(), request.numDisks(), request.size());
        List<String> diskPaths = boxManager.createSharedStorage(request.machineName(), request.numDisks(), request.size());
        return new VBoxMessage.CreateSharedStorageResponse(diskPaths);
    }
}
