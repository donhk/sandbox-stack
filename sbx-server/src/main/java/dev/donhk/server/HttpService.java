package dev.donhk.server;

import org.apache.pekko.actor.ActorRef;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.donhk.database.DBService;
import dev.donhk.config.Config;
import dev.donhk.web.handler.*;
import dev.donhk.web.rest.network.CreateNatNetwork;
import dev.donhk.web.rest.network.CreatePortForwardRule;
import dev.donhk.web.rest.network.GetNatNetwork;
import dev.donhk.web.rest.network.UpdatePortForwardRule;
import dev.donhk.web.rest.observability.GetOperationState;
import dev.donhk.web.rest.storage.CreateStorageUnits;
import dev.donhk.web.rest.storage.GetStorageUnits;
import dev.donhk.web.rest.ux.*;
import dev.donhk.web.rest.vm.*;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.javalin.router.JavalinDefaultRoutingApi;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class HttpService {

    private final ActorRef vboxActor;
    private final Config config;
    private final DBService db;

    public HttpService(Config config, DBService db, ActorRef vboxActor) {
        this.db = db;
        this.config = config;
        this.vboxActor = vboxActor;
    }

    public void startServer() {
        Logger.info("Creating instance of HttpServer ");

        Javalin app = Javalin.create(cfg -> {
            cfg.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/";
                staticFileConfig.directory = "/public";
                staticFileConfig.location = Location.CLASSPATH;
            });
            JavalinJackson mapper = new JavalinJackson();
            mapper.updateMapper(m -> {
                m.registerModule(new Jdk8Module());
                m.registerModule(new JavaTimeModule());
                m.enable(SerializationFeature.INDENT_OUTPUT);
            });
            cfg.jsonMapper(mapper);

            // CORS setup
            cfg.routes.before(this::corsOrigins);
            cfg.routes.options("/*", ctx -> {
                corsOrigins(ctx);
                ctx.status(204);
            });

            // Default route
            cfg.routes.get("/", new FrontEnd());

            // REST API endpoints
            vmOperations(cfg.routes);
            ux(cfg.routes);
            networkMode(cfg.routes);
            storageOperations(cfg.routes);
            observability(cfg.routes);
        });

        Logger.info("Sandboxer Service Started at http://0.0.0.0:{}", config.sbxServicePort);
        app.start(config.sbxServicePort);
    }

    private void corsOrigins(@NotNull Context ctx) {
        String origin = ctx.header("Origin");
        if (origin != null && config.corsOrigins.contains(origin)) {
            ctx.header("Access-Control-Allow-Origin", origin);
        }
        ctx.header("Vary", "Origin");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void ux(JavalinDefaultRoutingApi app) {
        // List machines
        app.get("/api/machines/list", new ListMachines(this.db));
        app.post("/api/machines/scan", new ScanMachines(this.vboxActor, this.db));
        app.get("/api/vm-seeds/list", new ListSeeds(this.db));
        app.get("/api/sbx-settings", new SbxSettings(this.config));
        app.get("/api/local-resources", new LocalResources(this.db));
        app.get("/api/local-vm-resources", new LocalVmResources(this.db));
    }

    private void vmOperations(JavalinDefaultRoutingApi app) {
        app.post("/api/machine", new CreateVm(this.vboxActor, this.db, this.config));
        app.get("/api/machine/{uuid}", new GetVm(this.db));
        app.post("/api/machine/start", new StartVm(this.vboxActor, this.db));
        app.put("/api/machine/pin", new PinVm(this.db));
        app.put("/api/machine", new UpdateVm(this.db));
        app.delete("/api/machine/{uuid}", new DeleteVm(this.vboxActor, this.db));
    }

    private void networkMode(JavalinDefaultRoutingApi app) {
        app.post("/api/nat-network", new CreateNatNetwork(this.vboxActor, this.db));
        app.get("/api/nat-network", new GetNatNetwork(this.db));
        app.post("/api/port-forward-rule", new CreatePortForwardRule(this.vboxActor, this.db, this.config));
        app.put("/api/port-forward-rule", new UpdatePortForwardRule(this.vboxActor, this.db));
    }

    private void storageOperations(JavalinDefaultRoutingApi app) {
        app.post("/api/storage-unit", new CreateStorageUnits(this.vboxActor, this.db));
        app.get("/api/storage-unit", new GetStorageUnits(this.db));
    }

    private void observability(JavalinDefaultRoutingApi app) {
        // Get operation state
        app.post("/api/operation/state", new GetOperationState());
        app.get("/api/ping", ctx -> ctx.result("pong"));

    }
}
