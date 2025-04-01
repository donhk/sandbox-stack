package dev.donhk.web.sbxControlls;

import dev.donhk.web.Renderer;
import io.javalin.http.Context;
import java.io.IOException;

public class Nothing implements WebCmd {
    @Override
    public void execute(Context ctx) throws IOException {
        Renderer.addHeaders("lost?", ctx);
    }
}
