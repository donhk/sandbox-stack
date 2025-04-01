package dev.donhk.web.sbxControlls;

import dev.donhk.sbx.ClientConnection;

import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import java.io.IOException;
import java.util.List;

import static dev.donhk.helpers.Constants.ADMIN_KILL;

public class GodKill implements WebCmd {

    private final List<ClientConnection> clientConnections;

    public GodKill(List<ClientConnection> clientConnections) {
        this.clientConnections = clientConnections;
    }

    @Override
    public void execute(Context ctx) throws IOException {
        //kill the specified vm
        for (ClientConnection conn : clientConnections) {
            conn.abortAndClean(ADMIN_KILL);
        }
        Renderer.addHeaders("all_machines_are_gone", ctx);
    }
}
