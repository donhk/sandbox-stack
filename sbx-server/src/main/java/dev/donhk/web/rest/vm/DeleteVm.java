package dev.donhk.web.rest.vm;

import akka.actor.ActorRef;
import dev.donhk.database.DBService;
import dev.donhk.database.DbUtils;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.types.Machine;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DeleteVm implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public DeleteVm(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }


    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String vmId = ctx.pathParam("uuid");
        TimeUnit.MILLISECONDS.sleep(10_000);
        Optional<MachineRow> machineRow = this.db.findMachine(vmId);
        if (machineRow.isEmpty()) {
            ctx.status(200).json(Map.of("uuid", vmId));
            return;
        }
        Machine machine = DbUtils.machineRow2Machine(this.db, machineRow.get());
        ctx.json(machine);
    }
}
