package dev.donhk.web.handler;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.MachineMeta;
import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.eclipse.jetty.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VMsMeta implements Handler {

    private final VMDataAccessService vmDataAccessService;

    public VMsMeta(VMDataAccessService vmDataAccessService) {
        this.vmDataAccessService = vmDataAccessService;
    }

    @Override
    public void handle(Context ctx) {
        Map<String, String> variables = new HashMap<>();

        // Inline resources
        variables.put("pure-min", "<style>" + Renderer.loadResource("public/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + Renderer.loadResource("public/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + Renderer.loadResource("public/js/ui.js") + "</script>");

        try {
            List<MachineMeta> info = vmDataAccessService.getMachinesMetaInfo();
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
                } else if (meta.comments.isEmpty()) {
                    sb.append("  <td>").append("&#127820;").append("</td>");
                } else {
                    sb.append("  <td><pre>").append(meta.comments).append("</pre></td>");
                }

                sb.append("</tr>");
            }

            variables.put("rows", sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            variables.put("rows", "<tr><td colspan=\"9\">Error loading data</td></tr>");
        }

        variables.put("main-content", Renderer.loadResource("public/views/VMsMetaPage.html"));
        String layoutHtml = Renderer.loadResource("public/views/Layout.html");
        String resultHtml = Renderer.processTemplate(layoutHtml, variables);

        ctx.status(HttpStatus.OK_200)
                .header("engine", "Sandboxer")
                .contentType("text/html")
                .result(resultHtml);
    }
}
