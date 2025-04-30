package dev.donhk.web.rest.ux;

import dev.donhk.database.DBService;
import dev.donhk.rest.types.VMSnapshot;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListSeeds implements Handler {

    private final DBService db;

    public ListSeeds(DBService db) {
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        List<VMSnapshot> snapshots = this.db.listSnapshots();
        ctx.json(snapshots);
    }
}
