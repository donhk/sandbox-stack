package dev.donhk.web.sbxControlls;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.Machine;

import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import org.eclipse.jetty.util.UrlEncoded;
import org.tinylog.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class UpdateVM implements WebCmd {
    private final VMDataAccessService VMDataAccessService;

    public UpdateVM(VMDataAccessService VMDataAccessService) {
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void execute(Context ctx) throws IOException {
        final String rawVm = ctx.req().getParameter("vm");
        final String vm = UrlEncoded.decodeString(rawVm).trim();
        try {
            final Machine activeVM = VMDataAccessService.findMachine(vm);
            if (activeVM != null) {
                final String adminMessage = VMDataAccessService.getVMMessage(vm);
                //update vm
                VMDataAccessService.pollMachine(vm);
                if (!adminMessage.isEmpty()) {
                    Renderer.addHeaders("msg" + adminMessage, ctx);
                } else {
                    Renderer.addHeaders("ok", ctx);
                }
            } else {
                Renderer.addHeaders("vm_is_gone", ctx);
            }
        } catch (SQLException e) {
            Logger.error("sql exception", e);
        }

    }
}
