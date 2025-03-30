package dev.donhk.web.sbxControlls;

import dev.donhk.database.VMDataAccessService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class VmsRunning implements WebCmd {
    private final VMDataAccessService VMDataAccessService;

    public VmsRunning(VMDataAccessService VMDataAccessService) {
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //respond how many vms are running
        try {
            resp.getWriter().print("{\"active_vms\":\"" + VMDataAccessService.getActiveMachines().size() + "\"}");
        } catch (SQLException e) {
            resp.getWriter().print("{\"active_vms\":\"-1\"}");
        }

    }
}
