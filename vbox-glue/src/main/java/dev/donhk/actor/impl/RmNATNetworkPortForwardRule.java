package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

public class RmNATNetworkPortForwardRule {

    private final VBoxManager boxManager;
    private final VBoxMessage.RmNATNetworkPortForwardRuleRequest request;

    public RmNATNetworkPortForwardRule(VBoxManager boxManager, VBoxMessage.RmNATNetworkPortForwardRuleRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.RmNATNetworkPortForwardRuleResponse dispatch() {
        Logger.info("RmNATNetworkPortForwardRuleRequest net={} rule={}", request.networkName(), request.ruleName());
        boxManager.rmNATNetworkPortForwardRule(request.networkName(), request.ruleName());
        return new VBoxMessage.RmNATNetworkPortForwardRuleResponse();
    }
}
