package dev.donhk.web.sbxControlls;

import dev.donhk.database.DBManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class VmsInfo implements WebCmd {
    private final DBManager dbManager;

    public VmsInfo(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String vm = req.getParameter("vm");
        //kill the specified vm
        Map<String, String> m;
        try {
            m = dbManager.getMetaDigestInfo();
            //update the history of updates dispatched
            dbManager.registerOperation(vm + "-update");
        } catch (SQLException e) {
            resp.getWriter().print("internal_error_call_support");
            return;
        }
        final Map.Entry<String, String> entry = m.entrySet().iterator().next();
        final String content = entry.getValue();
        resp.getWriter().print(content);
    }
}
