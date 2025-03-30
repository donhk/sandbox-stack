package dev.donhk.web.sbxControlls;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.Machine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class UpdateVM implements WebCmd {
    private final VMDataAccessService VMDataAccessService;

    public UpdateVM(VMDataAccessService VMDataAccessService) {
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String vm = req.getParameter("vm");
        try {
            final Machine activeVM = VMDataAccessService.findMachine(vm);
            if (activeVM != null) {
                final String adminMessage = VMDataAccessService.getVMMessage(vm);
                //update vm
                VMDataAccessService.pollMachine(vm);
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
