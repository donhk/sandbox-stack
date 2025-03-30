package dev.donhk.web.controlls;

import dev.donhk.helpers.Utils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static dev.donhk.system.VMMetadataSynchronizer.instructions;
import static dev.donhk.vbox.Constants.NA;

public class ReloadVMsMeta extends HttpServlet {

    private final Map<String, String> variables = new HashMap<>();
    private String secret = null;
    private String status = "&#127814;";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (secret == null) {
            secret = String.valueOf(System.currentTimeMillis());
        }

        if (req.getParameter("update") != null && req.getParameter("update").equals(secret)) {
            try {
                instructions.put(new Object());
                //refresh the key
                secret = String.valueOf(System.currentTimeMillis());
                //change the status
                status = "&#128571;";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        variables.put("pure-min", "<style>" + loadResource("web/css/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + loadResource("web/css/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + loadResource("web/js/ui.js") + "</script>");
        variables.put("main-content", loadResource("web/views/ReloadVMsPage.html"));
        variables.put("status", status);
        variables.put("secret", secret);

        //kind of cached result
        String content = loadResource("web/views/Layout.html");
        byte[] response = content.getBytes();
        resp.setStatus(HttpStatus.OK_200);
        resp.getWriter().println(new String(response));
        status = "&#127814;";
    }

    private String loadResource(String viewName) {
        try {
            return process(Utils.resource2txt(viewName));
        } catch (IOException e) {
            return NA;
        }
    }

    private String process(String source) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            source = source.replaceAll("\\{" + key + "}", value);
        }
        return source;
    }
}
