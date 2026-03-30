package dev.donhk.web.rest.vm;

import dev.donhk.database.DBService;
import dev.donhk.rest.operations.vm.UpdateMachineRequest;
import dev.donhk.rest.operations.vm.UpdateMachineResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;

public class UpdateVm implements Handler {

    private final DBService db;

    public UpdateVm(DBService db) {
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        UpdateMachineRequest request = ctx.bodyAsClass(UpdateMachineRequest.class);
        Logger.info("UpdateVm request: {}", request);

        if (db.findMachine(request.uuid()).isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        db.updateMachineTimestamp(request.uuid());
        ctx.json(new UpdateMachineResponse());
    }
}
