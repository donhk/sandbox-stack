package dev.donhk.web.rest.storage;

import dev.donhk.database.DBService;
import dev.donhk.rest.storage.GetStorageUnitsResponse;
import dev.donhk.rest.types.StorageUnit;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.List;

public class GetStorageUnits implements Handler {

    private final DBService db;

    public GetStorageUnits(DBService db) {
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String uuid = ctx.queryParam("uuid");
        Logger.info("GetStorageUnits uuid={}", uuid);

        if (uuid == null) {
            ctx.status(400).json(Collections.emptyMap());
            return;
        }

        if (db.findMachine(uuid).isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        List<StorageUnit> units = db.listStorageDisks(uuid);
        ctx.json(new GetStorageUnitsResponse(units));
    }
}
