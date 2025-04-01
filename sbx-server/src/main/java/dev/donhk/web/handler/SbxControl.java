package dev.donhk.web.handler;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.Tasks;
import dev.donhk.sbx.ClientConnection;
import dev.donhk.web.sbxControlls.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.eclipse.jetty.http.HttpStatus;

import java.util.List;

public class SbxControl implements Handler {

    private final VMDataAccessService vmDataAccessService;
    private final List<ClientConnection> clientConnections;

    public SbxControl(VMDataAccessService vmDataAccessService, List<ClientConnection> clientConnections) {
        this.vmDataAccessService = vmDataAccessService;
        this.clientConnections = clientConnections;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        WebCmd cmd = getWebCmd(ctx);
        ctx.status(HttpStatus.OK_200);
        cmd.execute(ctx);
    }

    private WebCmd getWebCmd(Context ctx) {
        Tasks task = findTask(ctx);
        return switch (task) {
            case UPDATE -> new UpdateVM(vmDataAccessService);
            case VMS_RUNNING -> new VmsRunning(vmDataAccessService);
            case KILL -> new KillVM(clientConnections);
            case GODKILL -> new GodKill(clientConnections);
            case VMS_INFO -> new VmsInfo(vmDataAccessService);
            case HELP -> new Help();
            case ADMIN_MSG -> new PostService(vmDataAccessService);
            default -> new Nothing();
        };
    }

    private Tasks findTask(Context ctx) {
        String taskParam = ctx.queryParam("task");
        if (taskParam == null) {
            return Tasks.NULL;
        }
        for (Tasks t : Tasks.values()) {
            if (t.getVal().equals(taskParam)) {
                return t;
            }
        }
        return Tasks.NULL;
    }
}