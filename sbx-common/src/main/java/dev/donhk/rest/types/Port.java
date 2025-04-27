package dev.donhk.rest.types;

public record Port(
        String name,
        int hostPort,
        int vmPort
) {}