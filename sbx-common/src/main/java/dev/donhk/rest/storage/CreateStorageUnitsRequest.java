package dev.donhk.rest.storage;

public record CreateStorageUnitsRequest(
        String uuid,
        long sizeBytes,
        int numDisks
) {}