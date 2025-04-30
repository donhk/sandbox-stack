package dev.donhk.server;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.donhk.database.DBService;
import dev.donhk.config.Config;
import dev.donhk.web.handler.*;
import dev.donhk.web.rest.observability.GetOperationState;
import dev.donhk.web.rest.ux.ListMachines;
import dev.donhk.web.rest.ux.ListSeeds;
import dev.donhk.web.rest.ux.SbxSettings;
import dev.donhk.web.rest.vm.DeleteVm;
import dev.donhk.web.rest.vm.GetVm;
import dev.donhk.web.rest.vm.PinVm;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
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

        Javalin app = Javalin.create(config -> {
                    config.staticFiles.add(
                            staticFileConfig -> {
                                staticFileConfig.hostedPath = "/";
                                staticFileConfig.directory = "/public";
                                staticFileConfig.location = Location.CLASSPATH;
                            }
                    );
                    JavalinJackson mapper = new JavalinJackson();
                    mapper.updateMapper(m -> {
                        m.registerModule(new Jdk8Module());
                        m.registerModule(new JavaTimeModule());
                        m.enable(SerializationFeature.INDENT_OUTPUT);
                    });
                    config.jsonMapper(mapper);
                }
        );

        // REST API endpoint
        vmOperations(app);
        ux(app);
        networkMode(app);
        storageOperations(app);
        observability(app);

        // Default route (optional)
        app.get("/", new FrontEnd());

        // CORS Setup
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://localhost:3000"); // Allow React dev server
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        // Respond to preflight OPTIONS requests
        app.options("/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://localhost:3000");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            ctx.status(204); // No content for preflight
        });

        Logger.info("Sandboxer Service Started at http://0.0.0.0:{}", config.sbxServicePort);
        app.start(config.sbxServicePort);
    }

    private void ux(Javalin app) {
        // List machines
        app.get("/api/machines/list", new ListMachines(this.db));
        app.get("/api/vm-seeds/list", new ListSeeds(this.db));
        app.get("/api/sbx-settings/list", new SbxSettings(this.config));
    }

    private void vmOperations(Javalin app) {
        // Create a machine
        app.post("/api/machine", ctx -> {
            // expects JSON body: CreateMachineRequest
        });

        // Get machine info
        app.get("/api/machine/{uuid}", new GetVm(this.db));

        // Start a machine
        app.post("/api/machine/start", ctx -> {
            // expects JSON body: StartMachineRequest
        });

        // Pin machine
        app.put("/api/machine/pin", new PinVm(this.db));

        // Update a machine
        app.put("/api/machine", ctx -> {
            // expects JSON body: UpdateMachineRequest
        });

        // Delete a machine
        app.delete("/api/machine/{uuid}", new DeleteVm(this.vboxActor, this.db));
    }

    private void networkMode(Javalin app) {
        // Create NAT network
        app.post("/api/nat-network", ctx -> {
            // expects JSON body: CreateNatNetworkRequest
        });

        // Get NAT network name
        app.get("/api/nat-network", ctx -> {
            // expects query param or JSON body: GetNatNetworkRequest
        });

        // Create port forward rule
        app.post("/api/port-forward-rule", ctx -> {
            // expects JSON body: CreatePortForwardRuleRequest
        });

        // Update port forward rule
        app.put("/api/port-forward-rule", ctx -> {
            // expects JSON body: UpdatePortForwardRuleRequest
        });

    }

    private void storageOperations(Javalin app) {
        // Create storage unit(s)
        app.post("/api/storage-unit", ctx -> {
            // expects JSON body: CreateStorageUnitsRequest
        });

        // Get storage unit(s)
        app.get("/api/storage-unit", ctx -> {
            // expects query param or JSON body: GetStorageUnitsRequest
        });

    }

    private void observability(Javalin app) {
        // Get operation state
        app.post("/api/operation/state", new GetOperationState());
        app.get("/api/ping", ctx -> ctx.result("pong"));

    }
}
