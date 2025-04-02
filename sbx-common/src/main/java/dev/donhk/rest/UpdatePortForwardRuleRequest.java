package dev.donhk.rest;

public record UpdatePortForwardRuleRequest(
        String uuid,
        int vmPort,
        String newRuleName
) {}