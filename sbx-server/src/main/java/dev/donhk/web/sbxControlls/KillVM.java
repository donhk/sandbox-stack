package dev.donhk.web.sbxControlls;

import dev.donhk.sbx.ClientConnection;
import org.eclipse.jetty.util.UrlEncoded;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static dev.donhk.helpers.Constants.ADMIN_KILL;

public class KillVM implements WebCmd {
    private final List<ClientConnection> clientConnections;

    public KillVM(List<ClientConnection> clientConnections) {
        this.clientConnections = clientConnections;
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getParameter("vm") == null) {
            resp.getWriter().print("Missing parameter vm");
            return;
        }

        final String vm = UrlEncoded.decodeString(req.getParameter("vm")).trim();

        //kill the specified vm
        for (ClientConnection conn : clientConnections) {
            if (conn.getName().equals(vm)) {
                conn.abortAndClean(ADMIN_KILL);
                resp.getWriter().print("vm " + vm + " successfully destroyed");
                return;
            }
        }
        resp.getWriter().print("vm_not_found");
    }
}
