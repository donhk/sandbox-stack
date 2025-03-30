package dev.donhk.server;

import dev.donhk.database.DatabaseServer;
import dev.donhk.helpers.Utils;
import dev.donhk.sbx.ClientConnection;
import dev.donhk.system.Postman;
import dev.donhk.system.VBoxNetsGarbageCollector;
import dev.donhk.system.VMMetadataSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class SandboxServer {


    private final Logger logger = LoggerFactory.getLogger(SandboxServer.class);

    //clients pool
    private final List<ClientConnection> clientConnections = new ArrayList<>();
    private boolean keepAlive = true;

    /**
     * Before run this method make sure you added the classes to the CLASSPATH
     * and the rmi registry is running listening on port 11000
     * (open the port on the firewall 11000/tcp)
     * <p>
     * export CLASSPATH=build
     * rmiregistry 11000
     *
     * @param args arguments
     */
    public void start(String[] args) {
        logger.info("Start sandboxer server");
        try {
            logger.info(System.lineSeparator() + Utils.resource2txt("build_info.properties"));
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        final DatabaseServer databaseServer = new DatabaseServer(dbuser, dbpass, dbname, webPort, tcpPort);
        Connection conn;
        try {
            logger.info("Start rdbms");
            databaseServer.startServer();
            conn = databaseServer.getDataSource();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        logger.info("systemTasks");
        systemTasks(conn);
        logger.info("webInterface");
        webInterface(conn);
        logger.info("sandboxerApp");
        sandboxerApp(conn);
    }

    private void webInterface(Connection conn) {
        final HttpDaemon http = new HttpDaemon(webUiPort, conn, clientConnections);
        try {
            http.startServer();
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void sandboxerApp(Connection conn) {
        new Thread(() -> {
            try {
                logger.info("starting sanboxer app listener");
                ServerSocket server = new ServerSocket(appPort);
                logger.info("Waiting connections");
                while (keepAlive) {
                    Socket socket = server.accept();
                    logger.info("New client");
                    //create new client connection
                    final ClientConnection clientConnection = new ClientConnection(socket, boxManager, conn);
                    //and start it up
                    try {
                        clientConnection.start();
                    } catch (Exception e) {
                        logger.info("There was a problem starting one client " + clientConnection.getName());
                    }
                    //added to our list of connection for later on maintenance
                    clientConnections.add(clientConnection);
                }
                server.close();
            } catch (Exception e) {
                keepAlive = false;
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void systemTasks(Connection conn) {
        VMMetadataSynchronizer.newInstance(conn, boxManager);
        VBoxNetsGarbageCollector.newInstance(conn, boxManager, clientConnections);
        Postman.newInstance(conn);
    }

}
