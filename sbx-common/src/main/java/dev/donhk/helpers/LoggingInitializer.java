package dev.donhk.helpers;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.tinylog.configuration.Configuration;

import java.util.logging.LogManager;

public class LoggingInitializer {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        // Redirect java.util.logging (JUL) to SLF4J
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Configuration.set("writer1", "console");
        Configuration.set("writer1.ansi", "true");
        Configuration.set("writer1.ansi.level", "info=green, warn=yellow, error=red");
        Configuration.set("format", "{date:yyyy-MM-dd HH:mm:ss} [{thread}] {class}.{method}() [{level}] {message}");

        Configuration.set("writer2", "file");
        Configuration.set("writer2.file", "logs/app.log");
        Configuration.set("level", "info");
        Configuration.set("format", "{date:yyyy-MM-dd HH:mm:ss} [{level}] ({class}.{method}) {message}");

        initialized = true;
    }

    // Optionally: allow reinitialization with custom file or level
    public static synchronized void init(String logFilePath, String level) {
        if (initialized) {
            return;
        }

        Configuration.set("writer", "file");
        Configuration.set("writer.file", logFilePath);
        Configuration.set("level", level);
        Configuration.set("format", "{date:yyyy-MM-dd HH:mm:ss} [{level}] ({class}.{method}) {message}");

        initialized = true;
    }
}