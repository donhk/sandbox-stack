package dev.donhk.web.rest.vm;

import org.apache.pekko.actor.ActorRef;
import dev.donhk.actors.Utilities;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.operations.vm.DeleteMachineResponse;
import dev.donhk.rest.types.NetworkType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.Optional;

public class DeleteVm implements Handler {

    private final ActorRef vboxActor;
    private final DBService db;

    public DeleteVm(ActorRef vboxActor, DBService db) {
        this.vboxActor = vboxActor;
        this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String vmId = ctx.pathParam("uuid");
        Optional<MachineRow> row = db.findMachine(vmId);
        if (row.isEmpty()) {
            ctx.status(404).json(Collections.emptyMap());
            return;
        }

        MachineRow machine = row.get();
        Logger.info("DeleteVm {} name={}", vmId, machine.name());

        // Power down and unregister the VM in VirtualBox
        Utilities.askSync(vboxActor, new VBoxMessage.CleanUpVMRequest(machine.name()));

        // Remove NAT network if this VM owned a shared one
        if (machine.networkType() == NetworkType.NAT && machine.network() != null) {
            VBoxMessage.RemoveNatNetworkResponse netResp = Utilities.askSync(vboxActor,
                    new VBoxMessage.RemoveNatNetworkRequest(machine.network()));
            Logger.info("RemoveNatNetwork {} success={}", machine.network(), netResp.success());
        }

        // Delete from DB (inserts history entry)
        db.deleteMachine(vmId);

        ctx.json(new DeleteMachineResponse(vmId));
    }
}
