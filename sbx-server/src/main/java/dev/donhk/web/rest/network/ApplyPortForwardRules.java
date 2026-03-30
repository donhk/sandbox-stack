package dev.donhk.web.rest.network;

import org.apache.pekko.actor.ActorRef;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.actors.Utilities;
import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.pojos.VMPortRow;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Applies stored port-forward rules to VirtualBox for a running VM.
 *
 * <p>Must be called after the VM has booted and received an IP address,
 * because the NAT network rule requires knowing the guest's IP.
 *
 * <p>POST /api/port-forward-rule/apply  body: {"uuid": "sbx..."}
 */
public class ApplyPortForwardRules implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public ApplyPortForwardRules(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String uuid = ctx.bodyAsClass(UuidBody.class).uuid();
        Logger.info("ApplyPortForwardRules uuid={}", uuid);

        Optional<MachineRow> machineOpt = db.findMachine(uuid);
        if (machineOpt.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        MachineRow machine = machineOpt.get();
        if (machine.network() == null) {
            Logger.warn("ApplyPortForwardRules: machine {} has no NAT network, skipping VBox rules", uuid);
            ctx.status(200);
            return;
        }

        List<VMPortRow> ports = db.listVmPorts(uuid);
        int applied = 0;
        for (VMPortRow port : ports) {
            VBoxMessage.AddNATNetworkPortForwardRuleResponse resp = Utilities.askSync(vboxActor,
                    new VBoxMessage.AddNATNetworkPortForwardRuleRequest(
                            machine.network(), port.hostPort(), port.vmPort(), machine.name(), port.name()));
            Logger.info("ApplyPortForwardRule {}: 0.0.0.0:{} -> {}:{} success={}",
                    port.name(), port.hostPort(), machine.name(), port.vmPort(), resp.success());
            if (resp.success()) applied++;
        }

        Logger.info("ApplyPortForwardRules uuid={}: applied {}/{} rules", uuid, applied, ports.size());
        ctx.status(200);
    }

    public record UuidBody(String uuid) {}
}
