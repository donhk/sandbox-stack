package dev.donhk.web.rest.ux;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.pojos.VMPortRow;
import dev.donhk.rest.types.Machine;
import dev.donhk.rest.types.Network;
import dev.donhk.rest.types.Port;
import dev.donhk.rest.types.StorageUnit;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListMachines implements Handler {

    private final VMDataAccessService vmDataAccessService;

    public ListMachines(VMDataAccessService vmDataAccessService) {
        this.vmDataAccessService = vmDataAccessService;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
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
        Logger.info("List machines started");
        List<Machine> machines = new ArrayList<>();
        List<MachineRow> machineRowList = vmDataAccessService.listAllVirtualMachines();
        Logger.info("Machines: " + machineRowList.size());
        for (MachineRow machineRow : machineRowList) {
            Logger.info("Getting machine ports");
            List<VMPortRow> vmPortRows = vmDataAccessService.listVmPorts(machineRow.uuid());
            Logger.info("VM ports: " + vmPortRows.size());
            List<StorageUnit> storageUnits = vmDataAccessService.listStorageDisks(machineRow.uuid());
            Logger.info("Storage disks: " + storageUnits.size());
            machines.add(new Machine(
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
            ));
        }

        ctx.json(machines);
    }
}
