package dev.donhk.web.rest.ux;

import dev.donhk.database.DBService;
import dev.donhk.rest.types.ResourceRow;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalResources implements Handler {

    private final DBService db;

    public LocalResources(DBService db) {
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        int daysBack = -1;
        int granularity = 10;
        List<ResourceRow> cpuRows = this.db.getLocalResources("cpu", granularity, daysBack);
        List<ResourceRow> ramRows = this.db.getLocalResources("ram", granularity, daysBack);
        List<ResourceRow> storageRows = this.db.getLocalResources("storage", granularity, daysBack);
        List<ResourceRow> networkRows = this.db.getLocalResources("network", granularity, daysBack);
        Map<String, List<ResourceRow>> rowMap = new HashMap<>();
        rowMap.put("cpu", cpuRows);
        rowMap.put("ram", ramRows);
        rowMap.put("storage", storageRows);
        rowMap.put("network", networkRows);
        ctx.json(rowMap);
    }
}
