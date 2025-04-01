package dev.donhk.web.sbxControlls;

import dev.donhk.database.VMDataAccessService;

import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import org.eclipse.jetty.util.UrlEncoded;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class VmsInfo implements WebCmd {
    private final VMDataAccessService VMDataAccessService;

    public VmsInfo(VMDataAccessService VMDataAccessService) {
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void execute(Context ctx) throws IOException {
        final String rawVm = ctx.req().getParameter("vm");
        final String vm = UrlEncoded.decodeString(rawVm).trim();
        //kill the specified vm
        Map<String, String> m;
        try {
            m = VMDataAccessService.getMetaDigestInfo();
            //update the history of updates dispatched
            VMDataAccessService.registerOperation(vm + "-update");
        } catch (SQLException e) {
            Renderer.addHeaders("internal_error_call_support", ctx);
            return;
        }
        final Map.Entry<String, String> entry = m.entrySet().iterator().next();
        final String content = entry.getValue();
        Renderer.addHeaders(content, ctx);
    }
}
