package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class AddNATNetworkPortForwardRule {

    private final VBoxManager boxManager;
    private final VBoxMessage.AddNATNetworkPortForwardRuleRequest request;

    public AddNATNetworkPortForwardRule(VBoxManager boxManager, VBoxMessage.AddNATNetworkPortForwardRuleRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.AddNATNetworkPortForwardRuleResponse dispatch() {
        Logger.info("AddNATNetworkPortForwardRuleRequest net={} host={} guest={} machine={} rule={}",
                request.networkName(), request.hostPort(), request.guestPort(), request.machineName(), request.ruleName());
        boolean success = boxManager.addNATNetworkPortForwardRule(
                request.networkName(), request.hostPort(), request.guestPort(), request.machineName(), request.ruleName());
        return new VBoxMessage.AddNATNetworkPortForwardRuleResponse(success);
    }
}
