package dev.donhk.web.handler;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.helpers.Utils;
import dev.donhk.pojos.DigestRow;
import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConfigFile implements Handler {

    private final VMDataAccessService vmDataAccessService;

    public ConfigFile(VMDataAccessService vmDataAccessService) {
        this.vmDataAccessService = vmDataAccessService;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        Map<String, String> variables = new HashMap<>();

        variables.put("pure-min", "<style>" + Renderer.loadResource("public/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + Renderer.loadResource("public/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + Renderer.loadResource("public/js/ui.js") + "</script>");

        try {
            DigestRow row = vmDataAccessService.getDigestRow();
            variables.put("config-file", "<pre>" + Utils.base64Decode(row.content) + "</pre>");
            variables.put("created", row.created);
            variables.put("digest", row.digest);
        } catch (SQLException e) {
            e.printStackTrace();
            variables.put("config-file", "<pre>Error loading config</pre>");
            variables.put("created", "N/A");
            variables.put("digest", "N/A");
        }

        variables.put("main-content", Renderer.loadResource("public/views/ConfigFilePage.html"));
        String layoutHtml = Renderer.loadResource("public/views/Layout.html");
        String resultHtml = Renderer.processTemplate(layoutHtml, variables);

        Renderer.addHeaders(resultHtml, ctx);
    }
}
