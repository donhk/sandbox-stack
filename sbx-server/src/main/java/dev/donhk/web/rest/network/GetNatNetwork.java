package dev.donhk.web.rest.network;

import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.network.GetNatNetworkResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.Optional;

public class GetNatNetwork implements Handler {

    private final DBService db;

    public GetNatNetwork(DBService db) {
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String uuid = ctx.queryParam("uuid");
        Logger.info("GetNatNetwork uuid={}", uuid);

        if (uuid == null) {
            ctx.status(400).json(Collections.emptyMap());
            return;
        }

        Optional<MachineRow> row = db.findMachine(uuid);
        if (row.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        ctx.json(new GetNatNetworkResponse(Optional.ofNullable(row.get().network())));
    }
}
