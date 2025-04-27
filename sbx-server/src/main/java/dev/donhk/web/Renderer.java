package dev.donhk.web;

import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;

import static dev.donhk.helpers.Constants.SBX_VERSION;
import static dev.donhk.vbox.Constants.NA;

public class Renderer {

    public static String loadResource(String path) {
        try (InputStream in = Renderer.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) return NA;
            return new String(in.readAllBytes());
        } catch (IOException e) {
            return NA;
        }
    }

    public static void addHeaders(String resultHtml, Context ctx) {
        ctx
                .status(HttpStatus.OK_200)
                .header("Server-Engine", "Sandboxer")
                .header("Server-Version", SBX_VERSION)
                .contentType("text/html")
                .result(resultHtml);
    }
}