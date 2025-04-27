package dev.donhk.rest.network;

public record CreatePortForwardRuleRequest(
        String uuid,
        int vmPort,
        String portName
) {}
