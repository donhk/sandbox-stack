package dev.donhk.rest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Machine(
        String uuid,
        String name,
        String seedName,
        String snapshot,
        Network network,
        Optional<String> ipAddress,
        List<Port> ports,
        String hostname,
        MachineState machineState,
        Instant createdAt,
        Instant seenAt,
        List<StorageUnit> storageUnits
) {}
