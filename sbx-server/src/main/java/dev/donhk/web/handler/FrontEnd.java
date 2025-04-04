package dev.donhk.web.handler;

import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class FrontEnd implements Handler {
    @Override
    public void handle(@NotNull Context context) {
        String html = Renderer.loadResource("public/index.html");
        Logger.info("message size {}", html.length());
        if (html.isEmpty()) {
            context.status(404).result("index.html not found");
            return;
        }
        // Set headers if needed
        Renderer.addHeaders(html, context);
    }
}
