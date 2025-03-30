package dev.donhk.web.sbxControlls;

import dev.donhk.database.VMDataAccessService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class VmsInfo implements WebCmd {
    private final VMDataAccessService VMDataAccessService;

    public VmsInfo(VMDataAccessService VMDataAccessService) {
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String vm = req.getParameter("vm");
        //kill the specified vm
        Map<String, String> m;
        try {
            m = VMDataAccessService.getMetaDigestInfo();
            //update the history of updates dispatched
            VMDataAccessService.registerOperation(vm + "-update");
        } catch (SQLException e) {
            resp.getWriter().print("internal_error_call_support");
            return;
        }
        final Map.Entry<String, String> entry = m.entrySet().iterator().next();
        final String content = entry.getValue();
        resp.getWriter().print(content);
    }
}
