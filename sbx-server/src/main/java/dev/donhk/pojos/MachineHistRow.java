package dev.donhk.pojos;

public class MachineHistRow {
    public final String machineName;
    public final String ipv4;
    public final String network;
    public final String rules;
    public final String created;
    public final String vm;
    public final String state;
    public final String destroyed;

    public MachineHistRow(String machineName,
                          String ipv4,
                          String network,
                          String rules,
                          String created,
                          String vm,
                          String state,
                          String destroyed) {
        this.machineName = machineName;
        this.ipv4 = ipv4;
        this.network = network;
        this.rules = rules;
        this.created = created;
        this.vm = vm;
        this.state = state;
        this.destroyed = destroyed;
    }
}
