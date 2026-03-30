package dev.donhk.web.rest.ux;

import org.apache.pekko.actor.ActorRef;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.actors.Utilities;
import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineMeta;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class ScanMachines implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public ScanMachines(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        Logger.info("ScanMachines: triggering VBox scan");

        VBoxMessage.ListMachinesResponse response =
                Utilities.askSync(vboxActor, new VBoxMessage.ListMachinesRequest());

        for (MachineMeta meta : response.machineMetas()) {
            try {
                db.upsertSeed(
                        meta.machinePrefix,
                        meta.machineName,
                        meta.user,
                        meta.password,
                        meta.home,
                        meta.snapshotName,
                        Integer.parseInt(meta.cpuCount),
                        Integer.parseInt(meta.memorySize),
                        meta.comments
                );
            } catch (Exception e) {
                Logger.error("Failed to upsert seed for prefix={}", meta.machinePrefix, e);
            }
        }
        Logger.info("ScanMachines: upserted {} seeds", response.machineMetas().size());

        ctx.status(200);
    }
}
