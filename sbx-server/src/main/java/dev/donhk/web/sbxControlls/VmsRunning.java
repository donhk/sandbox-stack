package dev.donhk.web.sbxControlls;

import dev.donhk.database.VMDataAccessService;

import dev.donhk.web.Renderer;
import io.javalin.http.Context;

import java.io.IOException;
import java.sql.SQLException;

public class VmsRunning implements WebCmd {
    private final VMDataAccessService VMDataAccessService;

    public VmsRunning(VMDataAccessService VMDataAccessService) {
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void execute(Context ctx) throws IOException {
        //respond how many vms are running
        try {
            Renderer.addHeaders("{\"active_vms\":\"" + VMDataAccessService.getActiveMachines().size() + "\"}", ctx);
        } catch (SQLException e) {
            Renderer.addHeaders("{\"active_vms\":\"-1\"}", ctx);
        }

    }
}
