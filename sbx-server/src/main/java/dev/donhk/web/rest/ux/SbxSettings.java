package dev.donhk.web.rest.ux;

import dev.donhk.config.Config;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class SbxSettings implements Handler {

    private final Config config;

    public SbxSettings(Config config) {
        this.config = config;
    }

    @Override
    public void handle(@NotNull Context ctx) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(300);
        ctx.json(this.config);
    }
}
