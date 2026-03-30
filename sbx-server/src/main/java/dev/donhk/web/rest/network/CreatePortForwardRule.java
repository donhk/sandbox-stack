package dev.donhk.web.rest.network;

import akka.actor.ActorRef;
import dev.donhk.actors.Utilities;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.config.Config;
import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.network.CreatePortForwardRuleRequest;
import dev.donhk.rest.network.CreatePortForwardRuleResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CreatePortForwardRule implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;
    private final Config config;

    public CreatePortForwardRule(ActorRef vboxActor, DBService db, Config config) {
        this.vboxActor = vboxActor;
        this.db = db;
        this.config = config;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        CreatePortForwardRuleRequest request = ctx.bodyAsClass(CreatePortForwardRuleRequest.class);
        Logger.info("CreatePortForwardRule uuid={} vmPort={} name={}", request.uuid(), request.vmPort(), request.portName());

        Optional<MachineRow> row = db.findMachine(request.uuid());
        if (row.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        MachineRow machine = row.get();

        // Pick a free host port from the configured pool
        Set<Integer> used = new HashSet<>(db.findUsedHostPorts());
        int hostPort = -1;
        for (int p = config.sbxServiceLowPort; p <= config.sbxServiceHighPort; p++) {
            if (!used.contains(p)) {
                hostPort = p;
                break;
            }
        }
        if (hostPort == -1) {
            ctx.status(503).json(Map.of("error", "No free host ports available"));
            return;
        }

        // Add the rule in VirtualBox (best-effort; VM may not be running yet)
        if (machine.network() != null) {
            VBoxMessage.AddNATNetworkPortForwardRuleResponse resp = Utilities.askSync(vboxActor,
                    new VBoxMessage.AddNATNetworkPortForwardRuleRequest(
                            machine.network(), hostPort, request.vmPort(), machine.name(), request.portName()));
            Logger.info("AddNATNetworkPortForwardRule success={}", resp.success());
        }

        db.insertVmPort(request.uuid(), request.portName(), hostPort, request.vmPort());

        ctx.status(201).json(new CreatePortForwardRuleResponse(hostPort));
    }
}
