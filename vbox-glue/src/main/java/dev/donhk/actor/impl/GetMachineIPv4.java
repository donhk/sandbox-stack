package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class GetMachineIPv4 {

    private final VBoxManager boxManager;
    private final VBoxMessage.GetMachineIPv4Request request;

    public GetMachineIPv4(VBoxManager boxManager, VBoxMessage.GetMachineIPv4Request request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.GetMachineIPv4Response dispatch() {
        Logger.info("GetMachineIPv4Request name={}", request.machineName());
        String ipv4 = boxManager.getMachineIPv4(request.machineName());
        return new VBoxMessage.GetMachineIPv4Response(ipv4);
    }
}
