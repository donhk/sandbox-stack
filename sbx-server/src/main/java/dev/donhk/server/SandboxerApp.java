package dev.donhk.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.actor.VBoxActor;
import dev.donhk.database.DatabaseServer;
import dev.donhk.helpers.Config;
import dev.donhk.system.Postman;
import dev.donhk.system.SystemCleaner;
import dev.donhk.system.SystemWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main class of the sandboxer service
 * this class starts the database and additional workers
 */
public class SandboxerApp {

    private final Logger logger = LoggerFactory.getLogger(SandboxerApp.class);
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

        logger.info("Start the system level threads");
        startSystemWorkers(dataSource);

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
        logger.info("Start database service");
        databaseServer.startServer();
        return databaseServer.getDataSource();
    }

    private void startSystemWorkers(HikariDataSource conn) {
        SystemWorker.newInstance(conn, vboxActor);
        SystemCleaner.newInstance(conn, boxManager, clientConnections);
        Postman.newInstance(conn);
    }
}
