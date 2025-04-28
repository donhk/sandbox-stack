package dev.donhk.database;

import dev.donhk.pojos.MachineRow;
import dev.donhk.pojos.VMPortRow;
import dev.donhk.rest.types.Machine;
import dev.donhk.rest.types.Network;
import dev.donhk.rest.types.Port;
import dev.donhk.rest.types.StorageUnit;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DbUtils {
    public static Machine machineRow2Machine(DBService db, MachineRow machineRow) throws SQLException {
        List<VMPortRow> vmPortRows = db.listVmPorts(machineRow.uuid());
        List<StorageUnit> storageUnits = db.listStorageDisks(machineRow.uuid());
        return new Machine(
                machineRow.uuid(),
                machineRow.name(),
                machineRow.seed_name(),
                machineRow.snapshot(),
                new Network(machineRow.networkType(), machineRow.network()),
                Optional.of(machineRow.vmIpAddress()),
                machineRow.hostname(),
                vmPortRows.stream().map(m -> new Port(m.name(), m.hostPort(), m.vmPort())).toList(),
                machineRow.vmHostname(),
                machineRow.machineState(),
                machineRow.createdAt(),
                machineRow.updatedAt(),
                storageUnits,
                machineRow.locked()
        );
    }
}
