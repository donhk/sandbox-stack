package dev.donhk.system;

import akka.actor.ActorRef;
import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.database.DBService;
import dev.donhk.config.Config;
import dev.donhk.config.ConfigFileFactory;
import dev.donhk.helpers.Utils;
import dev.donhk.pojos.MachineMeta;
import org.tinylog.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static dev.donhk.actors.Utilities.askSync;

/**
 * SystemWorker is responsible for refreshing and synchronizing the metadata of virtual machines.
 * <p>
 * This includes:
 * - Extracting the latest metadata from available VMs via VirtualBox actor
 * - Updating the VM metadata in the database
 * - Re-generating and updating the meta-info configuration file in the database
 * </p>
 * A background thread listens for trigger signals through a shared blocking queue (`instructions`)
 * to initiate metadata refreshes on demand.
 */
public class VMMetadataSynchronizer {

    private final ActorRef vboxActor;
    private final DBService DBService;

    /**
     * Shared blocking queue to receive refresh instructions from other modules.
     * TODO: Refactor to avoid static access and instead use direct instance communication.
     */
    public static BlockingQueue<Object> instructions = new ArrayBlockingQueue<>(100);

    /**
     * Constructs a new SystemWorker instance with database and actor references.
     *
     * @param conn      HikariCP database connection pool
     * @param vboxActor Reference to the VirtualBox actor for metadata extraction
     * @param config    Application configuration
     */
    private VMMetadataSynchronizer(HikariDataSource conn, ActorRef vboxActor, Config config) {
        this.vboxActor = vboxActor;
        this.DBService = new DBService(conn, config);
    }

    /**
     * Entry point to create and run a new SystemWorker instance.
     *
     * @param conn      HikariCP database connection pool
     * @param vboxActor Reference to the VirtualBox actor
     * @param config    Application configuration
     */
    public static void newInstance(HikariDataSource conn, ActorRef vboxActor, Config config) {
        final VMMetadataSynchronizer instance = new VMMetadataSynchronizer(conn, vboxActor, config);
        instance.run();
    }

    /**
     * Starts the initial metadata refresh and spawns a background thread
     * that listens for refresh signals on the blocking queue.
     */
    private void run() {
        executeMetadataRefresh();

        new Thread(() -> {
            Logger.info("Starting thread to wait for metadata refresh signals");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Logger.info("Metadata refresh thread waiting for signal...");
                    instructions.take(); // Blocking call
                    Logger.info("Signal received. Executing metadata refresh...");
                    executeMetadataRefresh();
                }
            } catch (InterruptedException e) {
                Logger.error("SystemWorker thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Refreshes the virtual machine metadata by:
     * - Extracting new VM metadata
     * - Updating VM records in the database
     * - Regenerating and storing the base64-encoded meta-info file and its digest
     */
    private void executeMetadataRefresh() {
        Logger.info("Extracting latest metadata from VMs");
        VBoxMessage.ListMachinesResponse resp = askSync(vboxActor, new VBoxMessage.ListMachinesRequest());
        List<MachineMeta> machines = resp.machineMetas();
        Logger.info("{} VM records found", machines.size());

        Logger.info("Updating VM metadata in the database");
        try {
            DBService.updateMachinesMeta(machines);
        } catch (SQLException e) {
            Logger.error("Error updating VM metadata in database", e);
            Thread.currentThread().interrupt();
        }

        Logger.info("Generating and updating meta-info file content");
        try {
            ConfigFileFactory factory = new ConfigFileFactory(machines);
            String content = Utils.base64Encode(factory.createMetaInfoFile());
            String digest = Utils.digest(content);
            DBService.updateMetaInfoFile(digest, content);
        } catch (SQLException e) {
            Logger.error("Error updating meta-info file content", e);
            Thread.currentThread().interrupt();
        }
    }
}
