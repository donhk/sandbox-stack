package dev.donhk.web.controlls;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.ActiveMachineRow;
import dev.donhk.web.core.Controller;

import java.sql.SQLException;
import java.util.List;

public class ActiveMachines extends Controller {

    private final VMDataAccessService VMDataAccessService;

    public ActiveMachines(VMDataAccessService VMDataAccessService) {
        super("public/views/Layout.html");
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void addVariables() {
        variables.put("pure-min", "<style>" + loadResource("public/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + loadResource("public/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + loadResource("public/js/ui.js") + "</script>");

        try {
            List<ActiveMachineRow> machines = VMDataAccessService.getActiveMachines();
            StringBuilder sb = new StringBuilder();
            for (ActiveMachineRow meta : machines) {
                sb.append("<tr>");
                sb.append("  <td>").append(meta.name).append("</td>");
                sb.append("  <td>").append(meta.ipv4).append("</td>");
                sb.append("  <td>").append(meta.state).append("</td>");
                sb.append("  <td>").append(meta.vm).append("</td>");
                sb.append("  <td>").append(meta.created).append("</td>");
                sb.append("  <td>").append(meta.network).append("</td>");
                sb.append("  <td>").append(meta.rules).append("</td>");
                sb.append("</tr>");
            }
            variables.put("rows", sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        variables.put("main-content", loadResource("public/views/ActiveVMsPage.html"));
    }
}
