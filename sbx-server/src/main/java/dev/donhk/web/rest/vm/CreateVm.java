package dev.donhk.web.rest.vm;

import org.apache.pekko.actor.ActorRef;
import dev.donhk.actors.Utilities;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.config.Config;
import dev.donhk.database.DBService;
import dev.donhk.rest.operations.vm.CreateMachineRequest;
import dev.donhk.rest.operations.vm.CreateMachineResponse;
import dev.donhk.rest.types.MachineState;
import dev.donhk.rest.types.NetworkType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.net.InetAddress;

public class CreateVm implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;
    private final Config config;

    public CreateVm(ActorRef vboxActor, DBService db, Config config) {
        this.vboxActor = vboxActor;
        this.db = db;
        this.config = config;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        CreateMachineRequest request = ctx.bodyAsClass(CreateMachineRequest.class);
        Logger.info("CreateVm request: {}", request);

        // Determine NAT network name (null means plain isolated NAT)
        String natNetwork = request.network()
                .filter(n -> n.networkType() == NetworkType.NAT || n.networkType() == NetworkType.INTERNAL_NETWORK)
                .map(dev.donhk.rest.types.Network::networkName)
                .orElse(null);

        // Create NAT network in VirtualBox if a shared network was requested
        if (natNetwork != null) {
            VBoxMessage.CreateNatNetworkResponse netResp = Utilities.askSync(vboxActor,
                    new VBoxMessage.CreateNatNetworkRequest(natNetwork));
            Logger.info("CreateNatNetwork {} success={}", natNetwork, netResp.success());
        }

        // Clone VM from seed snapshot
        VBoxMessage.CloneMachineResponse cloneResp = Utilities.askSync(vboxActor,
                new VBoxMessage.CloneMachineRequest(request.seedName(), request.snapshot(), request.name(), natNetwork));
        if (!cloneResp.success()) {
            ctx.status(400).json(java.util.Map.of("error", "Failed to clone machine from seed"));
            return;
        }

        NetworkType networkType = request.network().map(dev.donhk.rest.types.Network::networkType).orElse(NetworkType.NAT);
        String hostname = InetAddress.getLocalHost().getHostName() + ":" + config.sbxServicePort;

        db.insertMachine(request.uuid(), request.name(), request.seedName(), request.snapshot(),
                natNetwork, networkType, hostname, MachineState.INITIALIZED);

        ctx.status(201).json(new CreateMachineResponse(request.uuid()));
    }
}
