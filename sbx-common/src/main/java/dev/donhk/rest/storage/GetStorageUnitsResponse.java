package dev.donhk.rest.storage;

import dev.donhk.rest.types.StorageUnit;

import java.util.List;

public record GetStorageUnitsResponse(
        List<StorageUnit> storageUnits
) {}