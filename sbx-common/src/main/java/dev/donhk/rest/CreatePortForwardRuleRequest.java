package dev.donhk.rest;

public record CreatePortForwardRuleRequest(
        String uuid,
        int vmPort,
        String portName
) {}
