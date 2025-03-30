package dev.donhk.web.sbxControlls;

import dev.donhk.pojos.Tasks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Help implements WebCmd {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for (Tasks tasks1 : Tasks.values()) {
            sb.append(tasks1.getHelp()).append("\n");
        }
        resp.getWriter().print(sb.toString());
    }
}
