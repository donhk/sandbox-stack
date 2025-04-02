package dev.donhk.rest;

import java.util.List;

public record GetStorageUnitsResponse(
        List<StorageUnit> storageUnits
) {}