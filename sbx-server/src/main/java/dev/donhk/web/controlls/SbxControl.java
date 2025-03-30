package dev.donhk.web.controlls;

import dev.donhk.database.DBManager;
import dev.donhk.pojos.Tasks;
import dev.donhk.sbx.ClientConnection;
import dev.donhk.web.sbxControlls.*;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class SbxControl extends HttpServlet {

    private final DBManager dbManager;
    private final List<ClientConnection> clientConnections;

    public SbxControl(DBManager dbManager, List<ClientConnection> clientConnections) {
        this.dbManager = dbManager;
        this.clientConnections = clientConnections;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpStatus.OK_200);
        final WebCmd cmd = getWebCmd(req);
        cmd.execute(req, resp);
    }

    private WebCmd getWebCmd(HttpServletRequest req) {
        switch (findTask(req)) {
            case UPDATE:
                return new UpdateVM(dbManager);
            case VMS_RUNNING:
                return new VmsRunning(dbManager);
            case KILL:
                return new KillVM(clientConnections);
            case GODKILL:
                return new GodKill(clientConnections);
            case VMS_INFO:
                return new VmsInfo(dbManager);
            case HELP:
                return new Help();
            case ADMIN_MSG:
                return new PostService(dbManager);
            case NULL:
            default:
                return new Nothing();
        }
    }

    private Tasks findTask(HttpServletRequest req) {
        if (req.getParameter("task") == null) {
            return Tasks.NULL;
        }
        for (Tasks tasks1 : Tasks.values()) {
            if (tasks1.getVal().equals(req.getParameter("task"))) {
                return tasks1;
            }
        }
        return Tasks.NULL;
    }

}
