package dev.donhk.web.sbxControlls;

import dev.donhk.database.DBManager;
import dev.donhk.pojos.Machine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class UpdateVM implements WebCmd {
    private final DBManager dbManager;

    public UpdateVM(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String vm = req.getParameter("vm");
        try {
            final Machine activeVM = dbManager.findMachine(vm);
            if (activeVM != null) {
                final String adminMessage = dbManager.getVMMessage(vm);
                //update vm
                dbManager.pollMachine(vm);
                if (adminMessage.length() > 0) {
                    resp.getWriter().print("msg" + adminMessage);
                } else {
                    resp.getWriter().print("ok");
                }
            } else {
                resp.getWriter().print("vm_is_gone");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
