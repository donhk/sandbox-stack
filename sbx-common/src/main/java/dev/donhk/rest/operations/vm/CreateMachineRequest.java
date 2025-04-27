package dev.donhk.rest.operations.vm;

import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.donhk.rest.types.Network;
import dev.donhk.rest.types.StorageUnit;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateMachineRequest(
        String uuid,
        String name,
        String seedName,
        String snapshot,
        Optional<Network> network,
        List<StorageUnit> storageUnits
) {}