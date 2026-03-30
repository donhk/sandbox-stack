package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class RmNATPortForwardRule {

    private final VBoxManager boxManager;
    private final VBoxMessage.RmNATPortForwardRuleRequest request;

    public RmNATPortForwardRule(VBoxManager boxManager, VBoxMessage.RmNATPortForwardRuleRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.RmNATPortForwardRuleResponse dispatch() {
        Logger.info("RmNATPortForwardRuleRequest machine={} rule={}", request.machineName(), request.ruleName());
        boxManager.rmNATPortForwardRule(request.machineName(), request.ruleName());
        return new VBoxMessage.RmNATPortForwardRuleResponse();
    }
}
