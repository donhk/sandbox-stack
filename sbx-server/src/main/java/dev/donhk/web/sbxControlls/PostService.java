package dev.donhk.web.sbxControlls;

import dev.donhk.database.DBManager;
import dev.donhk.pojos.ActiveMachineRow;
import org.eclipse.jetty.util.UrlEncoded;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PostService implements WebCmd {

    private final DBManager dbManager;

    public PostService(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String target = req.getParameter("target");
        final String message = UrlEncoded.decodeString(req.getParameter("message"));

        if (target == null || message == null) {
            resp.getWriter().print("wrong_message_format");
            return;
        }

        try {
            if (target.equals("all_vms")) {
                final List<ActiveMachineRow> activeMachineRows = dbManager.getActiveMachines();
                int total = 0;
                for (ActiveMachineRow row : activeMachineRows) {
                    dbManager.insertAdminMessage(message, row.name);
                    total++;
                }
                resp.getWriter().print(total + "_messages_delivered");
            } else {
                if (!dbManager.machineExists(target)) {
                    resp.getWriter().print("wrong_target");
                } else {
                    dbManager.insertAdminMessage(message, target);
                    resp.getWriter().print("message_delivered");
                }
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }

    }
}
