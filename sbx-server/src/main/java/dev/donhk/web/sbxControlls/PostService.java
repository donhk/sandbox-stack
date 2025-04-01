package dev.donhk.web.sbxControlls;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.ActiveMachineRow;
import dev.donhk.web.Renderer;

import io.javalin.http.Context;
import org.eclipse.jetty.util.UrlEncoded;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PostService implements WebCmd {

    private final VMDataAccessService VMDataAccessService;

    public PostService(VMDataAccessService VMDataAccessService) {
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void execute(Context ctx) throws IOException {
        final String target = ctx.req().getParameter("target");
        final String message = ctx.req().getParameter("message");

        if (target == null || message == null) {
            Renderer.addHeaders("wrong_message_format", ctx);
            return;
        }
        final String decMessage = UrlEncoded.decodeString(message);
        try {
            if (target.equals("all_vms")) {
                final List<ActiveMachineRow> activeMachineRows = VMDataAccessService.getActiveMachines();
                int total = 0;
                for (ActiveMachineRow row : activeMachineRows) {
                    VMDataAccessService.insertAdminMessage(decMessage, row.name);
                    total++;
                }
                Renderer.addHeaders(total + "_messages_delivered", ctx);
            } else {
                if (!VMDataAccessService.machineExists(target)) {
                    Renderer.addHeaders("wrong_target", ctx);
                } else {
                    VMDataAccessService.insertAdminMessage(decMessage, target);
                    Renderer.addHeaders("message_delivered", ctx);
                }
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }

    }
}
