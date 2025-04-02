package dev.donhk.rest;

public record CreateStorageUnitsRequest(
        String uuid,
        long sizeBytes,
        int numDisks
) {}