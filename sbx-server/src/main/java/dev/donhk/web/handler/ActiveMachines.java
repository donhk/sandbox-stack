package dev.donhk.web.handler;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.pojos.ActiveMachineRow;
import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveMachines implements Handler {

    private final VMDataAccessService vmDataAccessService;

    public ActiveMachines(VMDataAccessService vmDataAccessService) {
        this.vmDataAccessService = vmDataAccessService;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        final Map<String, String> variables = new HashMap<>();

        variables.put("pure-min", "<style>" + Renderer.loadResource("public/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + Renderer.loadResource("public/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + Renderer.loadResource("public/js/ui.js") + "</script>");

        List<ActiveMachineRow> machines = vmDataAccessService.getActiveMachines();

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

        variables.put("main-content", Renderer.loadResource("public/views/ActiveVMsPage.html"));

        String layoutHtml = Renderer.loadResource("public/views/Layout.html");
        String resultHtml = Renderer.processTemplate(layoutHtml, variables);

        Renderer.addHeaders(resultHtml, ctx);
    }
}
