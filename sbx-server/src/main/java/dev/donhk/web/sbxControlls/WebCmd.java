package dev.donhk.web.sbxControlls;

import io.javalin.http.Context;
import java.io.IOException;

public interface WebCmd {
    void execute(Context ctx) throws IOException;
}
