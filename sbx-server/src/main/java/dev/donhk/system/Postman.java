package dev.donhk.system;

import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.database.VMDataAccessService;
import dev.donhk.config.Config;
import org.tinylog.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@code Postman} class is responsible for periodically cleaning up
 * previously sent messages from the database.
 * <p>
 * It uses a {@link ScheduledExecutorService} to schedule the cleanup task
 * every 12 hours, starting 1 hour after initialization.
 * </p>
 *
 * <p>This class is typically initialized once during application startup
 * and operates in the background without blocking the main thread.</p>
 */
public class Postman {

    /**
     * Data access service for VM-related operations
     */
    private final VMDataAccessService VMDataAccessService;

    /**
     * Private constructor to initialize the Postman with the required
     * data source and configuration.
     *
     * @param dataSource the HikariCP data source
     * @param config     application configuration
     */
    private Postman(HikariDataSource dataSource, Config config) {
        this.VMDataAccessService = new VMDataAccessService(dataSource, config);
    }

    /**
     * Creates and starts a new {@code Postman} instance.
     * <p>This schedules the message purging task to run in the background.</p>
     *
     * @param dataSource the HikariCP data source
     * @param config     application configuration
     */
    public static void newInstance(HikariDataSource dataSource, Config config) {
        final Postman postman = new Postman(dataSource, config);
        postman.run();
    }

    /**
     * Schedules the {@code cleanMessages} task to run every 12 hours,
     * starting 1 hour after this method is called.
     */
    private void run() {
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanMessages, 1L, 12L, TimeUnit.HOURS);
    }

    /**
     * Performs the actual purging of sent messages from the database.
     * This method is called periodically by the scheduler.
     */
    private void cleanMessages() {
        Logger.info("Purging messages");
        VMDataAccessService.purgeSentMessages();
    }
}