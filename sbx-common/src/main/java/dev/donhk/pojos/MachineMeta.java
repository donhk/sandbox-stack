package dev.donhk.pojos;

public class MachineMeta {

    public final String machinePrefix;
    public final String machineName;
    public final String snapshotName;
    public final String cpuCount;
    public final String memorySize;
    public final String user;
    public final String password;
    public final String home;
    public final String comments;

    public MachineMeta(
            String machinePrefix,
            String machineName,
            String snapshotName,
            long cpuCount,
            long memorySize,
            String user,
            String password,
            String home,
            String comments
    ) {
        this.machinePrefix = machinePrefix;
        this.machineName = machineName;
        this.snapshotName = snapshotName;
        this.cpuCount = String.valueOf(cpuCount);
        this.memorySize = String.valueOf(memorySize);
        this.user = user;
        this.password = password;
        this.home = home;
        this.comments = comments;
    }

    public MachineMeta(
            String machinePrefix,
            String machineName,
            String snapshotName,
            String cpuCount,
            String memorySize,
            String user,
            String password,
            String home,
            String comments
    ) {
        this.machinePrefix = machinePrefix;
        this.machineName = machineName;
        this.snapshotName = snapshotName;
        this.cpuCount = cpuCount;
        this.memorySize = memorySize;
        this.user = user;
        this.password = password;
        this.home = home;
        this.comments = comments;
    }
}
