package dev.donhk.system;

import dev.donhk.database.DBManager;
import dev.donhk.pojos.ActiveMachineRow;
import dev.donhk.sbx.ClientConnection;
import dev.donhk.vbox.VBoxManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.INATNetwork;
import org.virtualbox_6_1.VirtualBoxManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemCleaner {

    private final Logger logger = LoggerFactory.getLogger(SystemCleaner.class);
    private final VBoxManager vBoxManager;
    private final DBManager dbManager;
    private final Pattern natShape = Pattern.compile("\\w+_\\w+");
    private final List<ClientConnection> clientConnections;

    private SystemCleaner(Connection conn, VirtualBoxManager boxManager, List<ClientConnection> clientConnections) {
        this.vBoxManager = new VBoxManager(boxManager);
        this.dbManager = new DBManager(conn);
        this.clientConnections = clientConnections;
    }

    public static void newInstance(Connection conn, VirtualBoxManager boxManager, List<ClientConnection> clientConnections) {
        SystemCleaner systemCleaner = new SystemCleaner(conn, boxManager, clientConnections);
        systemCleaner.run();
    }

    private void run() {
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanTrash, 1L, 10L, TimeUnit.MINUTES);
    }

    private void cleanTrash() {
        logger.info("Looking for dangling networks");
        final List<INATNetwork> networks = vBoxManager.getNatNetworks();
        for (INATNetwork network : networks) {
            final String netName = network.getNetworkName();
            final Matcher matcher = natShape.matcher(netName);
            //does it look like a network that we created?
            if (!matcher.find()) {
                //no, skip it
                continue;
            }
            //yes, then check if it is in use
            try {
                final List<ActiveMachineRow> machines = dbManager.getActiveMachines();
                if (checkIfNetworkIsFree(netName, machines)) {
                    dropNetwork(netName);
                }
            } catch (SQLException e) {
                logger.warn(e.getMessage(), e);
            } catch (Exception e) {
                logger.warn("Unexpected error while purging network " + netName + " " + e.getMessage(), e);
            }
        }
        removeOldInstances();
    }

    private void removeOldInstances() {
        logger.info("Removing old instances, initial size [" + clientConnections.size() + "]");
        clientConnections.removeIf(connection -> !connection.isAlive());
        logger.info("[" + clientConnections.size() + "] after clean");
    }

    private boolean checkIfNetworkIsFree(String netName, List<ActiveMachineRow> machines) {
        for (ActiveMachineRow activeVM : machines) {
            if (activeVM.network.equals(netName)) {
                //the network is still in use, thus we cannot drop it
                logger.info(netName + " cannot be dropped because it is un use by " + activeVM.name);
                return false;
            }
        }
        return true;
    }

    private void dropNetwork(String netName) throws Exception {
        //this network is not longer in use
        logger.info("Purging dangling network " + netName);
        dbManager.dropNATNetwork(netName);
        vBoxManager.removeNatNetwork(netName);
    }
}
