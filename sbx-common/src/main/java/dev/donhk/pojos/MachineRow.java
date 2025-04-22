package dev.donhk.pojos;

import dev.donhk.rest.MachineState;
import dev.donhk.rest.NetworkType;

import java.sql.Timestamp;

public record MachineRow(
        String uuid,
        String name,
        String seed_name,
        String snapshot,
        String network,
        NetworkType networkType,
        String vmIpAddress,
        String hostname,
        String vmHostname,
        MachineState machineState,
        Timestamp createdAt,
        Timestamp updatedAt,
        boolean locked
) {
}
