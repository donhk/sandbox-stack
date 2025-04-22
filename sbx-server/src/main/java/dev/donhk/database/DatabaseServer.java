//http://halyph.com/blog/2015/01/22/how-to-use-embedded-h2-with-h2-console.html
package dev.donhk.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.config.Config;
import dev.donhk.helpers.Utils;
import dev.donhk.pojos.HostPortStatus;
import org.h2.tools.Server;
import org.tinylog.Logger;

import java.io.IOException;
import java.sql.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseServer {

    private HikariDataSource dataSource;
    private Server webServer = null;
    private Server tcpServer = null;

    private final boolean reset;
    private final CountDownLatch latch = new CountDownLatch(2);
    private final AtomicBoolean startError = new AtomicBoolean(false);
    private final Config config;

    public DatabaseServer(Config config, boolean reset) {
        this.config = config;
        this.reset = reset;
    }

    public DatabaseServer(Config config) {
        this(config, true);
    }

    /**
     * Starts the embedded H2 database server (both web and TCP servers), initializes the connection pool,
     * and sets up the database schema.
     * <p>
     * This method performs the following steps:
     * </p>
     * <ol>
     *   <li>Starts the H2 web console server on the configured port in a separate thread, allowing remote access.</li>
     *   <li>Starts the H2 TCP server on the configured port in a separate thread, allowing external JDBC clients to connect.</li>
     *   <li>Waits for both servers to finish starting using a {@link CountDownLatch}.</li>
     *   <li>If either server fails to start, throws a {@link SQLException}.</li>
     *   <li>Initializes the HikariCP connection pool.</li>
     *   <li>Initializes or resets the database schema based on the configuration.</li>
     * </ol>
     *
     * @throws SQLException         if either the web or TCP server fails to start or if schema initialization fails.
     * @throws IOException          if reading schema SQL scripts fails.
     * @throws InterruptedException if the current thread is interrupted while waiting for the servers to start.
     */
    public void startServer() throws SQLException, IOException, InterruptedException {
        new Thread(() -> {
            try {
                webServer = Server.createWebServer(
                        "-webAllowOthers",
                        "-webPort", String.valueOf(this.config.webPort)
                );
                webServer.start();
            } catch (SQLException e) {
                Logger.error("Error starting Database Web server", e);
                this.startError.set(true);
            } finally {
                latch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                tcpServer = Server.createTcpServer(
                        "-tcpAllowOthers",
                        "-tcpPort", String.valueOf(this.config.tcpPort),
                        "-ifNotExists"
                );
                tcpServer.start();
            } catch (SQLException e) {
                Logger.error("Error starting Database TCP server", e);
                this.startError.set(true);
            } finally {
                latch.countDown();
            }
        }).start();
        //wait until the servers are created
        latch.await();
        if (this.startError.get()) {
            throw new SQLException("Error starting database");
        }

        setupConnectionPool();
        // Run after pool is created
        //initDBSchema();
    }

    /**
     * Configures and initializes the HikariCP connection pool for the embedded H2 database.
     * <p>
     * This method constructs the JDBC URL using the active TCP server and the configured database name,
     * logs the endpoint, and sets up the {@link HikariDataSource} with the following parameters:
     * </p>
     * <ul>
     *   <li>JDBC URL: constructed from the H2 TCP server and database name</li>
     *   <li>Username and password from the application config</li>
     *   <li>Maximum pool size: 10</li>
     *   <li>Minimum idle connections: 3</li>
     *   <li>Connection timeout: 600,000 ms (10 minutes)</li>
     *   <li>Idle timeout: 60,000 ms (1 minute)</li>
     * </ul>
     * <p>
     * The resulting {@code HikariDataSource} is stored in the {@code dataSource} field for use throughout
     * the application.
     * </p>
     */
    private void setupConnectionPool() {
        String jdbcUrl = String.format("jdbc:h2:%s/./%s", tcpServer.getURL(), this.config.dbName);
        Logger.info("jdbc connection endpoint: {}", jdbcUrl);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(this.config.dbUser);
        hikariConfig.setPassword(this.config.dbPass);
        hikariConfig.setLeakDetectionThreshold(15_000);
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setMinimumIdle(3);
        hikariConfig.setConnectionTimeout(600_000);
        hikariConfig.setIdleTimeout(60_000);

        dataSource = new HikariDataSource(hikariConfig);
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Initializes the H2 database schema.
     * <p>
     * If the {@code reset} flag is enabled, the existing schema is dropped by executing the
     * {@code dropschema.sql} script. Then, the schema is (re)created by executing the {@code schema.sql} script.
     * </p>
     * <p>
     * After initializing the schema, the method populates the {@code hostport} table with port numbers ranging
     * from {@code sbxServiceLowPort} (inclusive) to {@code sbxServiceHighPort} (exclusive), setting their status
     * to {@code FREE}.
     * </p>
     *
     * @throws SQLException if a database access error occurs or SQL statements fail to execute.
     * @throws IOException  if there is an error reading the SQL script resources.
     */
    private void initDBSchema() throws SQLException, IOException {
        try (Connection conn = getDataSource().getConnection()) {
            if (reset) {
                Logger.info("rdbms schema refresh");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(Utils.resource2txt("dropschema.sql"));
                }
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(Utils.resource2txt("schema.sql"));
            }

            for (int port = this.config.sbxServiceLowPort; port < this.config.sbxServiceHighPort; port++) {
                String sql = "merge into hostport (port,status) values (?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, port);
                    ps.setString(2, HostPortStatus.FREE.name());
                    ps.executeUpdate();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void stopServer() {
        if (tcpServer != null) {
            System.out.println("stopping tcpServer");
            tcpServer.stop();
        }

        if (webServer != null) {
            System.out.println("stopping webServer");
            webServer.stop();
        }

        if (dataSource != null) {
            dataSource.close();
        }
    }
}
