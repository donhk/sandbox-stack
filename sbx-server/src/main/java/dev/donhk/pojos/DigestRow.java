package dev.donhk.pojos;

public class DigestRow {
    public final String digest;
    public final String created;
    public final String content;

    public DigestRow(String digest, String created, String content) {
        this.digest = digest;
        this.created = created;
        this.content = content;
    }
}
