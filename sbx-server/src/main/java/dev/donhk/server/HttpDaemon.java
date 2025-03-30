package dev.donhk.server;

import dev.donhk.database.DBManager;
import dev.donhk.sbx.ClientConnection;
import dev.donhk.web.core.WebContextsHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class HttpDaemon {

    private final Logger logger = LoggerFactory.getLogger(HttpDaemon.class);
    private final int port;
    private final String address;
    private final DBManager dbManager;
    private Server server = null;
    private final List<ClientConnection> clientConnections;

    public HttpDaemon(int port, Connection conn, List<ClientConnection> clientConnections) {
        this.port = port;
        this.dbManager = new DBManager(conn);
        this.clientConnections = clientConnections;
        address = "http://localhost:" + port;
    }

    public void startServer() {
        logger.info("Creating instance of HttpServer ");
        if (server == null) {
            server = new Server(port);
            final ServletContextHandler handler = new ServletContextHandler(server, "/");
            final WebContextsHandler webContextsHandler = new WebContextsHandler(dbManager, clientConnections);

            logger.info("Binding contexts");
            //bind web contexts
            for (Map.Entry<String, ServletHolder> e : webContextsHandler.getContexts().entrySet()) {
                handler.addServlet(e.getValue(), e.getKey());
                logger.info("new Context: " + e.getKey());
            }

            logger.info("Starting server");
            try {
                server.start();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            logger.info("web server started at: " + address);
        } else {
            logger.info("The web server is already running at: " + address);
        }

    }

    public void stopServer() {
        logger.info("Stopping HttpServer " + address);
        if (server != null) {
            try {
                server.stop();
            } catch (Exception ignored) {
            }
        }
    }
}
