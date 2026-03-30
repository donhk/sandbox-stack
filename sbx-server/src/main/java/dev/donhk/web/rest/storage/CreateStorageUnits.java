package dev.donhk.web.rest.storage;

import akka.actor.ActorRef;
import dev.donhk.actors.Utilities;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.storage.CreateStorageUnitsRequest;
import dev.donhk.rest.storage.CreateStorageUnitsResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CreateStorageUnits implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public CreateStorageUnits(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        CreateStorageUnitsRequest request = ctx.bodyAsClass(CreateStorageUnitsRequest.class);
        Logger.info("CreateStorageUnits uuid={} numDisks={} sizeBytes={}", request.uuid(), request.numDisks(), request.sizeBytes());

        Optional<MachineRow> row = db.findMachine(request.uuid());
        if (row.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        String machineName = row.get().name();
        int sizeGB = (int) (request.sizeBytes() / (1024L * 1_000_000L));

        VBoxMessage.CreateSharedStorageResponse storageResp = Utilities.askSync(vboxActor,
                new VBoxMessage.CreateSharedStorageRequest(machineName, request.numDisks(), sizeGB));
        if (storageResp.diskPaths() == null) {
            ctx.status(400).json(Map.of("error", "Failed to create shared storage"));
            return;
        }

        List<String> diskPaths = storageResp.diskPaths();
        VBoxMessage.AddSharedStorageToMachineResponse attachResp = Utilities.askSync(vboxActor,
                new VBoxMessage.AddSharedStorageToMachineRequest(machineName, diskPaths));
        Logger.info("AddSharedStorageToMachine success={}", attachResp.success());

        db.insertStorageUnits(request.uuid(), diskPaths, request.sizeBytes());

        ctx.status(201).json(new CreateStorageUnitsResponse(request.uuid()));
    }
}
