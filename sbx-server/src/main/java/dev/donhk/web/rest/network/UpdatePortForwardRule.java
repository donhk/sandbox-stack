package dev.donhk.web.rest.network;

import akka.actor.ActorRef;
import dev.donhk.actors.Utilities;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.pojos.VMPortRow;
import dev.donhk.rest.network.UpdatePortForwardRuleRequest;
import dev.donhk.rest.network.UpdatePortForwardRuleResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.Optional;

public class UpdatePortForwardRule implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public UpdatePortForwardRule(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        UpdatePortForwardRuleRequest request = ctx.bodyAsClass(UpdatePortForwardRuleRequest.class);
        Logger.info("UpdatePortForwardRule uuid={} vmPort={} newName={}", request.uuid(), request.vmPort(), request.newRuleName());

        Optional<MachineRow> machineRow = db.findMachine(request.uuid());
        if (machineRow.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        Optional<VMPortRow> portRow = db.findVmPort(request.uuid(), request.vmPort());
        if (portRow.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        MachineRow machine = machineRow.get();
        VMPortRow port = portRow.get();

        // Replace the VBox rule with the new name, keeping the same ports
        if (machine.network() != null) {
            Utilities.askSync(vboxActor,
                    new VBoxMessage.RmNATNetworkPortForwardRuleRequest(machine.network(), port.name()));
            VBoxMessage.AddNATNetworkPortForwardRuleResponse resp = Utilities.askSync(vboxActor,
                    new VBoxMessage.AddNATNetworkPortForwardRuleRequest(
                            machine.network(), port.hostPort(), port.vmPort(), machine.name(), request.newRuleName()));
            Logger.info("UpdatePortForwardRule re-add success={}", resp.success());
        }

        db.updateVmPortName(request.uuid(), request.vmPort(), request.newRuleName());

        ctx.json(new UpdatePortForwardRuleResponse());
    }
}
