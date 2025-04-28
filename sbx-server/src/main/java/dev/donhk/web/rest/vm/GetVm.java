package dev.donhk.web.rest.vm;

import dev.donhk.database.DBService;
import dev.donhk.database.DbUtils;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.types.Machine;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class GetVm implements Handler {

    private final DBService db;

    public GetVm(DBService db) {
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String vmId = ctx.pathParam("uuid");
        MachineRow row = this.db.findMachine(vmId);
        Machine machine = DbUtils.machineRow2Machine(this.db, row);
        ctx.json(machine);
    }
}
