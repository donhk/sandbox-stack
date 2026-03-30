package dev.donhk.web.rest.vm;

import org.apache.pekko.actor.ActorRef;
import dev.donhk.actors.Utilities;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.operations.vm.StartMachineRequest;
import dev.donhk.rest.operations.vm.StartMachineResponse;
import dev.donhk.rest.types.MachineState;
import dev.donhk.vbox.LaunchMode;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.Optional;

public class StartVm implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public StartVm(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        StartMachineRequest request = ctx.bodyAsClass(StartMachineRequest.class);
        Logger.info("StartVm request: {}", request);

        Optional<MachineRow> row = db.findMachine(request.uuid());
        if (row.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        MachineRow machine = row.get();
        VBoxMessage.LaunchMachineResponse launchResp = Utilities.askSync(vboxActor,
                new VBoxMessage.LaunchMachineRequest(machine.name(), LaunchMode.headless));
        Logger.info("LaunchMachine {} success={}", machine.name(), launchResp.success());

        db.updateMachineState(request.uuid(), MachineState.BOOTING);

        ctx.json(new StartMachineResponse(request.uuid()));
    }
}
