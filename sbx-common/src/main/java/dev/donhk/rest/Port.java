package dev.donhk.rest;

public record Port(
        String name,
        int hostPort,
        int vmPort
) {}