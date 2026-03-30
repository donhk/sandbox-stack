package dev.donhk.web.rest.network;

import org.apache.pekko.actor.ActorRef;
import dev.donhk.actors.Utilities;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.network.CreateNatNetworkRequest;
import dev.donhk.rest.network.CreateNatNetworkResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.Optional;

public class CreateNatNetwork implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public CreateNatNetwork(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        CreateNatNetworkRequest request = ctx.bodyAsClass(CreateNatNetworkRequest.class);
        Logger.info("CreateNatNetwork request uuid={}", request.uuid());

        Optional<MachineRow> row = db.findMachine(request.uuid());
        if (row.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        String networkName = row.get().network();
        if (networkName == null) {
            ctx.status(400).json(java.util.Map.of("error", "Machine has no network assigned"));
            return;
        }

        VBoxMessage.CreateNatNetworkResponse resp = Utilities.askSync(vboxActor,
                new VBoxMessage.CreateNatNetworkRequest(networkName));
        Logger.info("CreateNatNetwork {} success={}", networkName, resp.success());

        ctx.status(201).json(new CreateNatNetworkResponse(request.uuid()));
    }
}
