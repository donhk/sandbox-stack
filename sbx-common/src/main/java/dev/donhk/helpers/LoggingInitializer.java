package dev.donhk.helpers;

import org.tinylog.configuration.Configuration;

public class LoggingInitializer {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        Configuration.set("writer", "file");
        Configuration.set("writer.file", "logs/app.log");
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