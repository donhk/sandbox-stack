package dev.donhk.web.rest.ux;

import dev.donhk.database.DBService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class VMResourcesChart implements Handler {

    private final DBService db;

    public VMResourcesChart(DBService db) {
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {

    }
}
