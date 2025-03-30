package dev.donhk.system;

import akka.actor.ActorRef;
import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.database.DBManager;
import dev.donhk.helpers.Config;
import dev.donhk.helpers.ConfigFileFactory;
import dev.donhk.helpers.Utils;
import dev.donhk.pojos.MachineMeta;
import dev.donhk.vbox.MetaExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This task is in charge of refreshing the existing vms metadata in the database which consyst on
 * load the latest info about the available virtual machines, Updating information in the database
 * Updating meta-info file content database
 */
public class SystemWorker {

    private final Logger logger = LoggerFactory.getLogger(SystemWorker.class);
    private final ActorRef vboxActor;
    private final DBManager dbManager;
    public static BlockingQueue<Object> instructions = new ArrayBlockingQueue<>(100);

    private SystemWorker(HikariDataSource conn, ActorRef vboxActor, Config config) {
        this.vboxActor = vboxActor;
        this.dbManager = new DBManager(conn, config);
    }

    public static void newInstance(HikariDataSource conn, ActorRef vboxActor, Config config) {
        final SystemWorker instance = new SystemWorker(conn, vboxActor, config);
        instance.run();
    }

    private void run() {
        executeMetadataRefresh();
        new Thread(() -> {
            logger.info("Starting thread to wait for changes");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    logger.info("Blocked on take");
                    Object o = instructions.take();
                    executeMetadataRefresh();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void executeMetadataRefresh() {
        //load the latest info about the available virtual machines
        logger.info("Getting latest meta-data from the VMs");
        MetaExtractor metaExtractor = new MetaExtractor(this.vboxActor);
        List<MachineMeta> machines = metaExtractor.genMetaInfo();
        logger.info(machines.size() + " registers found");
        logger.info("Updating information in the database");
        try {
            dbManager.updateMachinesMeta(machines);
        } catch (SQLException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        logger.info("Updating meta-info file content database");
        try {
            ConfigFileFactory factory = new ConfigFileFactory(machines);
            String content = Utils.base64Encode(factory.createMetaInfoFile());
            String digest = Utils.digest(content);
            dbManager.updateMetaInfoFile(digest, content);
        } catch (SQLException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
