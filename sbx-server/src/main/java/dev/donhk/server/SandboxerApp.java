package dev.donhk.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.actor.VBoxActor;
import dev.donhk.database.DBService;
import dev.donhk.database.DatabaseServer;
import dev.donhk.config.Config;
import dev.donhk.helpers.LoggingInitializer;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main class of the sandboxer service
 * this class starts the database and additional workers
 */
public class SandboxerApp {

    static {
        LoggingInitializer.init();
    }

    private final Config config;
    private final ActorRef vboxActor;

    private SandboxerApp(Config config) {
        this.config = config;
        final ActorSystem system = ActorSystem.create(VBoxActor.id);
        vboxActor = system.actorOf(Props.create(VBoxActor.class), VBoxActor.id);
    }

    public static SandboxerApp newInstance(String[] args) {
        Config app = new Config();
        CommandLine cmd = new CommandLine(app);
        cmd.execute(args);
        return new SandboxerApp(app);
    }

    public void start() throws SQLException, IOException, InterruptedException {
        // start the database server
        final HikariDataSource dataSource = this.startDatabaseService();

        Logger.info("Start the system level threads");
        //startSystemWorkers(dataSource);
        Logger.info("Start the web interface");
        startHttpEndpoints(dataSource);
    }

    private void startHttpEndpoints(HikariDataSource conn) {
        final DBService db = new DBService(conn, config);
        final HttpService http = new HttpService(this.config, db, this.vboxActor);
        http.startServer();
    }

    /**
     * Initializes and starts the embedded H2 database service using {@link DatabaseServer}.
     * <p>
     * This method creates a new {@code DatabaseServer} instance with the current configuration,
     * starts both the web and TCP servers, initializes the connection pool, and sets up the schema.
     * </p>
     * <p>
     * Once the database service is fully started, it returns the {@link HikariDataSource} instance
     * for obtaining connections from the connection pool.
     * </p>
     *
     * @return the {@link HikariDataSource} used to access the running database.
     * @throws SQLException         if starting the database server or setting up the schema fails.
     * @throws IOException          if loading SQL schema scripts fails.
     * @throws InterruptedException if the thread is interrupted while waiting for the server to start.
     */
    private HikariDataSource startDatabaseService() throws SQLException, IOException, InterruptedException {
        final DatabaseServer databaseServer = new DatabaseServer(this.config);
        Logger.info("Start database service");
        databaseServer.startServer();
        return databaseServer.getDataSource();
    }
}
