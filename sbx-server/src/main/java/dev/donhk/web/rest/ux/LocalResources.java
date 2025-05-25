package dev.donhk.web.rest.ux;

import dev.donhk.database.DBService;
import dev.donhk.database.DbUtils;
import dev.donhk.rest.types.ResourceRow;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        int granularityMin = 60;
        int limit = 24;
        List<ResourceRow> cpuRows = this.db.getLocalResources("cpu", granularityMin, daysBack, limit);
        List<ResourceRow> ramRows = this.db.getLocalResources("ram", granularityMin, daysBack, limit);
        List<ResourceRow> storageRows = this.db.getLocalResources("storage", granularityMin, daysBack, limit);
        List<ResourceRow> networkRows = this.db.getLocalResources("network", granularityMin, daysBack, limit);

        Map<String, List<String>> payload = new HashMap<>();
        List<String> timestamps = new ArrayList<>(cpuRows.size());
        for (ResourceRow cpu : cpuRows) {
            timestamps.add(DbUtils.formatDayHourMin(cpu.timestamp()));
        }

        payload.put("timestamps", timestamps);
        payload.put("cpu", toList(cpuRows));
        payload.put("ram", toList(ramRows));
        payload.put("storage", toList(storageRows));
        payload.put("network", toList(networkRows));

        ctx.json(payload);
    }

    List<String> toList(List<ResourceRow> rows) {
        List<String> strLogs = new ArrayList<>(rows.size());
        for (ResourceRow row : rows) {
            strLogs.add(String.valueOf(row.usage()));
        }
        return strLogs;
    }
}
