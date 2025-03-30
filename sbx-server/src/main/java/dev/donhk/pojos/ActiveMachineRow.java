package dev.donhk.pojos;

public class ActiveMachineRow {
    public final String name;
    public final String ipv4;
    public final String state;
    public final String vm;
    public final String created;
    public final String network;
    public final String rules;

    public ActiveMachineRow(
            String name,
            String ipv4,
            String state,
            String vm,
            String created,
            String network,
            String rules
    ) {
        this.name = name;
        this.ipv4 = ipv4;
        this.state = state;
        this.vm = vm;
        this.created = created;
        this.network = network;
        this.rules = rules;
    }
}
