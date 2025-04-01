package dev.donhk.web.sbxControlls;

import dev.donhk.pojos.Tasks;

import dev.donhk.web.Renderer;
import io.javalin.http.Context;

import java.io.IOException;

public class Help implements WebCmd {
    @Override
    public void execute(Context ctx) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for (Tasks tasks : Tasks.values()) {
            sb.append(tasks.getHelp()).append("\n");
        }
        Renderer.addHeaders(sb.toString(), ctx);
    }
}
