package dev.donhk.web.sbxControlls;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface WebCmd {
    void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
