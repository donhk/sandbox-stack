package dev.donhk.server;

import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.database.VMDataAccessService;
import dev.donhk.helpers.Config;
import dev.donhk.sbx.ClientConnection;
import dev.donhk.web.handler.*;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;

public class HttpDaemon {

    private final int port;
    private final String address;
    private final VMDataAccessService vmDataAccessService;
    private final List<ClientConnection> clientConnections;

    public HttpDaemon(Config config, HikariDataSource conn, List<ClientConnection> clientConnections) {
        this.port = config.sbxServiceUIPort;
        this.vmDataAccessService = new VMDataAccessService(conn, config);
        this.clientConnections = clientConnections;
        address = "http://localhost:" + port;
    }

    public void startServer() {
        Logger.info("Creating instance of HttpServer ");
        Javalin app = Javalin.create(config ->
                config.staticFiles.add(
                        staticFileConfig -> {
                            staticFileConfig.hostedPath = "/";
                            staticFileConfig.directory = "/public";
                            staticFileConfig.location = Location.CLASSPATH;
                        }
                )).start(port);

        Logger.info("web server started at: {}", address);

        // REST API endpoint
        app.get("/api/hello", ctx -> ctx.json(Map.of("message", "Hello from REST API!")));

        // Default route (optional)
        app.get("/", new ActiveMachines(vmDataAccessService));
        app.get("/meta", new VMsMeta(vmDataAccessService));
        app.get("/config", new ConfigFile(vmDataAccessService));
        app.get("/stats", new UsageStats(vmDataAccessService));
        app.get("/reload", new ReloadVMsMeta());
        app.get("/sbx", new SbxControl(vmDataAccessService, clientConnections));

    }
}
