package dev.donhk.rest.types;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.ResultSet;
import java.sql.SQLException;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VMSnapshot(
        String prefix,
        String vm_user,
        String vm_pass,
        String home,
        String snapshot_name,
        int snapshot_cpus,
        int snapshot_ram_mb,
        String snapshot_comments
) {
    public static VMSnapshot fromResultSet(ResultSet rs) throws SQLException {
        return new VMSnapshot(
                rs.getString("prefix"),
                rs.getString("vm_user"),
                rs.getString("vm_pass"),
                rs.getString("home"),
                rs.getString("snapshot_name"),
                rs.getInt("snapshot_cpus"),
                rs.getInt("snapshot_ram_mb"),
                rs.getString("snapshot_comments")
        );
    }
}
