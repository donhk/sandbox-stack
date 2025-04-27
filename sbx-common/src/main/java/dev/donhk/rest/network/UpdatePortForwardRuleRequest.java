package dev.donhk.rest.network;

public record UpdatePortForwardRuleRequest(
        String uuid,
        int vmPort,
        String newRuleName
) {}