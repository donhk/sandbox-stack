package dev.donhk.system;

import dev.donhk.database.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Postman {

    private final Logger logger = LoggerFactory.getLogger(Postman.class);
    private final DBManager dbManager;

    private Postman(Connection conn) {
        this.dbManager = new DBManager(conn);
    }

    public static void newInstance(Connection conn) {
        final Postman postman = new Postman(conn);
        postman.run();
    }

    private void run() {
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanMessages, 1L, 12L, TimeUnit.HOURS);
    }

    private void cleanMessages() {
        logger.info("Purging messages");
        dbManager.purgeSentMessages();
    }
}
