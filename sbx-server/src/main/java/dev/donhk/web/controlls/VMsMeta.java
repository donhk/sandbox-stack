package dev.donhk.web.controlls;

import dev.donhk.database.DBManager;
import dev.donhk.pojos.MachineMeta;
import dev.donhk.web.core.Controller;

import java.sql.SQLException;
import java.util.List;

public class VMsMeta extends Controller {

    private final DBManager dbManager;

    public VMsMeta(DBManager dbManager) {
        super("web/views/Layout.html");
        this.dbManager = dbManager;
    }

    @Override
    public void addVariables() {
        variables.put("pure-min", "<style>" + loadResource("web/css/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + loadResource("web/css/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + loadResource("web/js/ui.js") + "</script>");

        try {
            List<MachineMeta> info = dbManager.getMachinesMetaInfo();
            StringBuilder sb = new StringBuilder();
            for (MachineMeta meta : info) {
                sb.append("<tr>");
                sb.append("  <td>").append(meta.machinePrefix).append("</td>");
                sb.append("  <td>").append(meta.machineName).append("</td>");
                sb.append("  <td>").append(meta.snapshotName).append("</td>");
                sb.append("  <td>").append(meta.cpuCount).append("</td>");
                sb.append("  <td>").append(meta.memorySize).append("</td>");
                sb.append("  <td>").append(meta.user).append("</td>");
                sb.append("  <td>").append(meta.password).append("</td>");
                sb.append("  <td><pre>").append(meta.home.replace("\\\\", "\\\\\\\\")).append("</pre></td>");
                if (meta.comments.length() > 20) {
                    String part = meta.comments.substring(0, 15) + "...";
                    sb.append("  <td><pre>").append(part).append("</pre></td>");
                } else if (meta.comments.length() == 0) {
                    sb.append("  <td>").append("&#127820;").append("</td>");
                }
                sb.append("</tr>");
            }
            variables.put("rows", sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        variables.put("main-content", loadResource("web/views/VMsMetaPage.html"));
    }
}