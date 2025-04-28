package dev.donhk.web.rest.ux;

import dev.donhk.database.DBService;
import dev.donhk.database.DbUtils;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.types.Machine;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListMachines implements Handler {

    private final DBService db;

    public ListMachines(DBService db) {
        this.db = db;
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
        List<Machine> machines = new ArrayList<>();
        for (MachineRow machineRow : db.listAllVirtualMachines()) {
            try {
                machines.add(DbUtils.machineRow2Machine(this.db, machineRow));
            } catch (SQLException e) {
                Logger.error("Error generating Machine from {}", machineRow, e);
            }
        }

        ctx.json(machines);
    }
}
