package dev.donhk.pojos;

import java.sql.Timestamp;

public class Machine {
    public final String name;
    public final String ipv4;
    public final String state;
    public final Timestamp created;
    public final int hostPort;
    public final int guestPort;
    public final String network;

    public Machine(String name, String ipv4, String state, Timestamp created, int hostPort, int guestPort, String network) {
        this.name = name;
        this.ipv4 = ipv4;
        this.state = state;
        this.created = created;
        this.hostPort = hostPort;
        this.guestPort = guestPort;
        this.network = network;
    }
}
