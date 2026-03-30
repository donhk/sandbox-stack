package dev.donhk.web.rest.vm;

import org.apache.pekko.actor.ActorRef;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.actors.Utilities;
import dev.donhk.database.DBService;
import dev.donhk.database.DbUtils;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.types.Machine;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.Optional;

public class GetVm implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public GetVm(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String vmId = ctx.pathParam("uuid");
        Optional<MachineRow> row = this.db.findMachine(vmId);
        if (row.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }
        MachineRow machineRow = row.get();

        // If the IP is not yet stored, ask VBox for it and persist it.
        if (machineRow.vmIpAddress() == null) {
            VBoxMessage.GetMachineIPv4Response ipResp = Utilities.askSync(vboxActor,
                    new VBoxMessage.GetMachineIPv4Request(machineRow.name()));
            if (ipResp.ipv4() != null && !ipResp.ipv4().isBlank()) {
                Logger.info("GetVm: resolved IP {} for machine {}", ipResp.ipv4(), vmId);
                db.updateMachineIp(vmId, ipResp.ipv4());
                machineRow = this.db.findMachine(vmId).orElse(machineRow);
            }
        }

        Machine machine = DbUtils.machineRow2Machine(this.db, machineRow);
        ctx.json(machine);
    }
}
