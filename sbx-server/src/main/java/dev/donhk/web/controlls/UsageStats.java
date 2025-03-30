package dev.donhk.web.controlls;

import dev.donhk.database.DBManager;
import dev.donhk.pojos.MachineHistRow;
import dev.donhk.web.core.Controller;

import java.sql.SQLException;
import java.util.List;

public class UsageStats extends Controller {

    private final DBManager dbManager;

    public UsageStats(DBManager dbManager) {
        super("web/views/Layout.html");
        this.dbManager = dbManager;
    }

    @Override
    public void addVariables() {
        variables.put("pure-min", "<style>" + loadResource("web/css/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + loadResource("web/css/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + loadResource("web/js/ui.js") + "</script>");

        try {

            int totalVMs = dbManager.getTotalMachinesHist();
            int totalUpdates = dbManager.getTotalUpdatesServed();

            variables.put("totalVMs", String.valueOf(totalVMs));
            variables.put("totalUpdates", String.valueOf(totalUpdates));

            List<MachineHistRow> rows = dbManager.getMachinesHistPreview();
            StringBuilder sb = new StringBuilder();
            for (MachineHistRow meta : rows) {
                sb.append("<tr>");
                sb.append("  <td>").append(meta.machineName).append("</td>");
                sb.append("  <td>").append(meta.ipv4).append("</td>");
                sb.append("  <td>").append(meta.network).append("</td>");
                sb.append("  <td>").append(meta.rules).append("</td>");
                sb.append("  <td>").append(meta.created).append("</td>");
                sb.append("  <td>").append(meta.vm).append("</td>");
                sb.append("  <td>").append(meta.state).append("</td>");
                sb.append("  <td>").append(meta.destroyed).append("</td>");
                sb.append("</tr>");
            }
            variables.put("rows", sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        variables.put("main-content", loadResource("web/views/UsageStatsPage.html"));
    }
}
