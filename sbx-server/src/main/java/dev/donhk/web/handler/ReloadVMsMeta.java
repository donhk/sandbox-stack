package dev.donhk.web.handler;

import dev.donhk.helpers.Utils;
import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static dev.donhk.system.VMMetadataSynchronizer.instructions;
import static dev.donhk.vbox.Constants.NA;

public class ReloadVMsMeta implements Handler {

    private String secret = null;
    private String status = "&#127814;"; // 🍆

    @Override
    public void handle(@NotNull Context ctx) {
        Map<String, String> variables = new HashMap<>();

        if (secret == null) {
            secret = String.valueOf(System.currentTimeMillis());
        }

        if ("true".equals(ctx.queryParam("update")) && secret.equals(ctx.queryParam("update"))) {
            try {
                instructions.put(new Object());
                secret = String.valueOf(System.currentTimeMillis());
                status = "&#128571;"; // 🐓
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        variables.put("pure-min", "<style>" + loadResource("public/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + loadResource("public/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + loadResource("public/js/ui.js") + "</script>");
        variables.put("main-content", loadResource("public/views/ReloadVMsPage.html"));
        variables.put("status", status);
        variables.put("secret", secret);

        String content = loadResource("public/views/Layout.html");
        String finalHtml = process(content, variables);

        Renderer.addHeaders(finalHtml, ctx);

        // Reset status for next render
        status = "&#127814;";
    }

    private String loadResource(String path) {
        try {
            return Utils.resource2txt(path);
        } catch (IOException e) {
            return NA;
        }
    }

    private String process(String source, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            source = source.replaceAll("\\{" + entry.getKey() + "}", entry.getValue());
        }
        return source;
    }
}