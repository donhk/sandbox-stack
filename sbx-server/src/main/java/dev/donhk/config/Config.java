package dev.donhk.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import picocli.CommandLine;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config implements Runnable {
    //
    // database information
    //
    @CommandLine.Option(names = {"-d", "--db-name"}, description = "database name")
    public String dbName = "sandbox";

    @JsonIgnore
    @CommandLine.Option(names = {"-u", "--db-pass"}, description = "database password")
    public String dbPass = "welcome";

    @JsonIgnore
    @CommandLine.Option(names = {"-p", "--db-user"}, description = "database username")
    public String dbUser = "dbmaster";

    @JsonIgnore
    @CommandLine.Option(names = {"-r", "--db-reset"}, description = "reset database schema")
    public boolean dbReset = false;

    @JsonIgnore
    @CommandLine.Option(names = {"-ss", "--sql-seeds"}, description = "insert SQL seeds")
    public boolean dbSqlSeed = false;

    @CommandLine.Option(names = {"-w", "--db-web-port"}, description = "database web port")
    public int webPort = 8082;

    @CommandLine.Option(names = {"-t", "--db-tcp-port"}, description = "database TCP port")
    public int tcpPort = 9094;

    //
    // Sandboxer service configurations
    //
    @CommandLine.Option(names = {"-s", "--service-port"}, description = "Sandboxer Service port")
    public int sbxServicePort = 8008;

    @CommandLine.Option(names = {"-l", "--service-low-port"}, description = "Sandboxer Service low port")
    public int sbxServiceLowPort = 11200;

    @CommandLine.Option(names = {"-h", "--service-high-port"}, description = "Sandboxer Service high port")
    public int sbxServiceHighPort = 11500;

    //
    // Build details
    //
    @CommandLine.Option(names = {"-b", "--build-info"}, description = "Build information")
    public String buildInfo = "";

    @Override
    public void run() {
    }
}
