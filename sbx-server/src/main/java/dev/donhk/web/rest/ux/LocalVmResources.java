package dev.donhk.web.rest.ux;

import dev.donhk.database.DBService;
import dev.donhk.rest.types.VmHistoryRow;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalVmResources implements Handler {

    private final DBService db;

    public LocalVmResources(DBService db) {
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        Map<String, List<String>> payload = new HashMap<>();
        int monthsBack = -12;
        VmHistoryRow vmHistoryRow = this.db.getVmsHistory(monthsBack, 12);
        payload.put("labels", vmHistoryRow.labels());
        payload.put("vmCounts", vmHistoryRow.vmCounts());
        ctx.json(payload);
    }
}
