package dev.donhk.pojos;

public class Rule {
    public final String rule_name;
    public final int hostport;
    public final int guestport;

    public Rule(String rule_name, int hostport, int guestport) {
        this.rule_name = rule_name;
        this.hostport = hostport;
        this.guestport = guestport;
    }
}
