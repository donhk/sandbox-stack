package dev.donhk.rest;

import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateMachineRequest(
        String uuid,
        String name,
        String seedName,
        String snapshot,
        Optional<Network> network,
        List<StorageUnit> storageUnits
) {}