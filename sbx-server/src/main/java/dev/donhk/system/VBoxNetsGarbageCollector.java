package dev.donhk.system;

import akka.actor.ActorRef;
import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.database.DBService;
import dev.donhk.config.Config;
import dev.donhk.pojos.ActiveMachineRow;
import org.tinylog.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dev.donhk.actors.Utilities.askSync;

/**
 * {@code VBoxNetsGarbageCollector} is a background task responsible for cleaning up
 * unused VirtualBox NAT networks and stale client connections from the system.
 * <p>
 * It performs two main functions on a scheduled interval:
 * <ul>
 *     <li>Identifies and removes "dangling" (unused) VirtualBox NAT networks from metadata</li>
 *     <li>Removes inactive client connections</li>
 * </ul>
 * <p>
 * The task is scheduled to run every 10 minutes, starting 1 minute after initialization.
 * </p>
 *
 * <p>This class is designed to be initialized once via the static {@code newInstance} method.</p>
 */
public class VBoxNetsGarbageCollector {

    private final DBService DBService;
    private final ActorRef vboxActor;

    /**
     * Constructs a new {@code VBoxNetsGarbageCollector}.
     *
     * @param conn      a JDBC connection pool (Hikari)
     * @param vboxActor reference to the VirtualBox actor
     * @param config    application configuration
     */
    private VBoxNetsGarbageCollector(HikariDataSource conn, ActorRef vboxActor, Config config) {
        this.vboxActor = vboxActor;
        this.DBService = new DBService(conn, config);
    }

    /**
     * Initializes and runs a new {@code VBoxNetsGarbageCollector} instance.
     *
     * @param conn      a JDBC connection pool (Hikari)
     * @param vboxActor reference to the VirtualBox actor
     * @param config    application configuration
     */
    public static void newInstance(HikariDataSource conn, ActorRef vboxActor, Config config) {
        VBoxNetsGarbageCollector VBoxNetsGarbageCollector = new VBoxNetsGarbageCollector(conn, vboxActor, config);
        VBoxNetsGarbageCollector.run();
    }

    /**
     * Schedules the {@code cleanTrash} task to run every 10 minutes,
     * with an initial delay of 1 minute.
     */
    private void run() {
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanTrash, 1L, 10L, TimeUnit.MINUTES);
    }

    /**
     * Cleans up unused VirtualBox networks and removes inactive client connections.
     * <p>
     * This includes:
     * <ul>
     *     <li>Fetching active VMs from the database</li>
     *     <li>Requesting the list of dangling networks from the VirtualBox actor</li>
     *     <li>Removing those networks from metadata</li>
     *     <li>Cleaning up dead client connections</li>
     * </ul>
     */
    private void cleanTrash() {
        Logger.info("Looking for dangling networks from VirtualBox");
        try {
            final List<ActiveMachineRow> machines = DBService.getActiveMachines();
            Logger.info("{} actives machines found", machines.size());
            final List<String> unusedNetworks = askSync(vboxActor, new VBoxMessage.DelDanglingNetsRequest(machines));
            Logger.info("{} dangling networks found", machines.size());
            for (String unusedNetwork : unusedNetworks) {
                try {
                    Logger.info("Removing network {} metadata from database", unusedNetwork);
                    DBService.dropNATNetwork(unusedNetwork);
                } catch (Exception e) {
                    Logger.warn("Unexpected error while purging network {} message {}", unusedNetwork, e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            Logger.warn(e.getMessage(), e);
        }
    }

}
