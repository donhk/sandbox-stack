package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

import java.util.List;

public class GetPortForwardRules {

    private final VBoxManager boxManager;
    private final VBoxMessage.GetPortForwardRulesRequest request;

    public GetPortForwardRules(VBoxManager boxManager, VBoxMessage.GetPortForwardRulesRequest request) {
        this.boxManager = boxManager;
        this.request = request;
    }

    public VBoxMessage.GetPortForwardRulesResponse dispatch() {
        Logger.info("GetPortForwardRulesRequest net={}", request.networkName());
        List<String> rules = boxManager.getPortForwardRules(request.networkName());
        return new VBoxMessage.GetPortForwardRulesResponse(rules);
    }
}
