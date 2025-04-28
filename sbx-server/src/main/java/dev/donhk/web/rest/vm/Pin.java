package dev.donhk.web.rest.vm;

import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.operations.vm.PinMachineRequest;
import dev.donhk.rest.operations.vm.PinMachineResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.concurrent.TimeUnit;

public class Pin implements Handler {

    private final DBService DBService;

    public Pin(DBService DBService) {
        this.DBService = DBService;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        PinMachineRequest request = ctx.bodyAsClass(PinMachineRequest.class);
        Logger.info("Request: {}", request);
        TimeUnit.MILLISECONDS.sleep(15_000);
        Logger.info("Locked: {} {} -> {}", request.uuid(), !request.locked(), request.locked());
        MachineRow row = this.DBService.updateVmLockState(request.uuid(), request.locked());
        ctx.json(new PinMachineResponse(request.uuid(), request.name(), request.network(), row.locked()));
    }
}
