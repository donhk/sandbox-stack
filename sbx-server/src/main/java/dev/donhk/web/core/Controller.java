package dev.donhk.web.core;

import dev.donhk.helpers.Utils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class Controller extends HttpServlet {

    protected Map<String, String> variables = new HashMap<>();
    private final String viewName;
    private boolean processed = false;
    private byte[] response;
    private int lastVariablesHash = variables.hashCode();

    public Controller(String viewName) {
        this.viewName = viewName;
    }

    public abstract void addVariables();

    protected String loadResource(String viewName) {
        try {
            return process(Utils.resource2txt(viewName));
        } catch (IOException e) {
            return "N/A";
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

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //kind of cached result
        if (!processed || lastVariablesHash != variables.hashCode()) {
            addVariables();
            //merge content in layout
            String content = loadResource(viewName);
            response = content.getBytes();
            processed = true;
        }
        resp.setStatus(HttpStatus.OK_200);
        resp.getWriter().println(new String(response));
    }
}
