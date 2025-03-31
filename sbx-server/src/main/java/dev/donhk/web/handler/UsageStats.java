package dev.donhk.web.handler;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.MachineHistRow;
import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.eclipse.jetty.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageStats implements Handler {

    private final VMDataAccessService vmDataAccessService;

    public UsageStats(VMDataAccessService vmDataAccessService) {
        this.vmDataAccessService = vmDataAccessService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        Map<String, String> variables = new HashMap<>();

        // Inline styles and scripts
        variables.put("pure-min", "<style>" + Renderer.loadResource("public/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + Renderer.loadResource("public/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + Renderer.loadResource("public/js/ui.js") + "</script>");

        try {
            int totalVMs = vmDataAccessService.getTotalMachinesHist();
            int totalUpdates = vmDataAccessService.getTotalUpdatesServed();

            variables.put("totalVMs", String.valueOf(totalVMs));
            variables.put("totalUpdates", String.valueOf(totalUpdates));

            List<MachineHistRow> rows = vmDataAccessService.getMachinesHistPreview();
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
        } catch (Exception e) {
            e.printStackTrace();
            variables.put("rows", "<tr><td colspan=\"8\">Error loading data</td></tr>");
        }

        variables.put("main-content", Renderer.loadResource("public/views/UsageStatsPage.html"));
        String layoutHtml = Renderer.loadResource("public/views/Layout.html");
        String resultHtml = Renderer.processTemplate(layoutHtml, variables);

        ctx.status(HttpStatus.OK_200)
                .header("engine", "Sandboxer")
                .contentType("text/html")
                .result(resultHtml);
    }
}
