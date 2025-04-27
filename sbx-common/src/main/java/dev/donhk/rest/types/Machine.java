package dev.donhk.rest.types;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;

/*
     {
     "uuid": "mch-018",
     "name": "Machine-R",
     "seedName": "seed-18",
     "snapshot": "snap-018",
     "network": "network3",
     "vmIpAddress": "192.168.10.18",
     "hostname": "local1.localhost:3999",
     "ports": [],
     "vmHostname": "machine-r",
     "machineState": "RUNNING",
     "createdAt": "2025-02-05T02:00:00Z",
     "updatedAt": "2025-04-01T02:20:00Z",
     "storageUnits": [],
     "locked": false,
 },
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Machine(
        String uuid,
        String name,
        String seedName,
        String snapshot,
        Network network,
        Optional<String> vmIpAddress,
        String hostname,
        List<Port> ports,
        String vmHostname,
        MachineState machineState,
        Timestamp createdAt,
        Timestamp updatedAt,
        List<StorageUnit> storageUnits,
        boolean locked
) {
}
