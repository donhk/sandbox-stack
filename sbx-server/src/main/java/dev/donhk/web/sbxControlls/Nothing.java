package dev.donhk.web.sbxControlls;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Nothing implements WebCmd {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().print("lost?");
    }
}
