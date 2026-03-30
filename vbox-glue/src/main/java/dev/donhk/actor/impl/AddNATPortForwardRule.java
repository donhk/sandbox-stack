package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class AddNATPortForwardRule {

    private final VBoxManager boxManager;
    private final VBoxMessage.AddNATPortForwardRuleRequest request;

    public AddNATPortForwardRule(VBoxManager boxManager, VBoxMessage.AddNATPortForwardRuleRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.AddNATPortForwardRuleResponse dispatch() {
        Logger.info("AddNATPortForwardRuleRequest host={} guest={} machine={} rule={}",
                request.hostPort(), request.guestPort(), request.machineName(), request.ruleName());
        boolean success = boxManager.addNATPortForwardRule(
                request.hostPort(), request.guestPort(), request.machineName(), request.ruleName());
        return new VBoxMessage.AddNATPortForwardRuleResponse(success);
    }
}
