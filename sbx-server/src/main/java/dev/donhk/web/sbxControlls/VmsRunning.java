package dev.donhk.web.sbxControlls;

import dev.donhk.database.DBManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class VmsRunning implements WebCmd {
    private final DBManager dbManager;

    public VmsRunning(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //respond how many vms are running
        try {
            resp.getWriter().print("{\"active_vms\":\"" + dbManager.getActiveMachines().size() + "\"}");
        } catch (SQLException e) {
            resp.getWriter().print("{\"active_vms\":\"-1\"}");
        }

    }
}
