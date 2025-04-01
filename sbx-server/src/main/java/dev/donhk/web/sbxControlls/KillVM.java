package dev.donhk.web.sbxControlls;

import dev.donhk.sbx.ClientConnection;
import dev.donhk.web.Renderer;
import org.eclipse.jetty.util.UrlEncoded;

import io.javalin.http.Context;

import java.io.IOException;
import java.util.List;

import static dev.donhk.helpers.Constants.ADMIN_KILL;

public class KillVM implements WebCmd {
    private final List<ClientConnection> clientConnections;

    public KillVM(List<ClientConnection> clientConnections) {
        this.clientConnections = clientConnections;
    }

    @Override
    public void execute(Context ctx) throws IOException {
        final String rawVm = ctx.req().getParameter("vm");
        if (rawVm == null) {
            Renderer.addHeaders("Missing parameter vm", ctx);
            return;
        }

        final String vm = UrlEncoded.decodeString(rawVm).trim();

        //kill the specified rawVm
        for (ClientConnection conn : clientConnections) {
            if (conn.getName().equals(vm)) {
                conn.abortAndClean(ADMIN_KILL);
                Renderer.addHeaders("vm " + vm + " successfully destroyed", ctx);
                return;
            }
        }
        Renderer.addHeaders("vm_not_found", ctx);
    }
}
