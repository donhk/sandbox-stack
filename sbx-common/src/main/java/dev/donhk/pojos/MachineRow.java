package dev.donhk.pojos;

import dev.donhk.rest.types.MachineState;
import dev.donhk.rest.types.NetworkType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public record MachineRow(
        String uuid,
        String name,
        String seed_name,
        String snapshot,
        String network,
        NetworkType networkType,
        String vmIpAddress,
        String hostname,
        String vmHostname,
        MachineState machineState,
        Timestamp createdAt,
        Timestamp updatedAt,
        boolean locked
) {
    public static MachineRow fromResultSet(ResultSet rs) throws SQLException {
        return new MachineRow(
                rs.getString("uuid"),
                rs.getString("name"),
                rs.getString("seed_name"),
                rs.getString("snapshot"),
                rs.getString("network"),
                NetworkType.valueOf(rs.getString("network_type")),
                rs.getString("vm_ip_address"),
                rs.getString("hostname"),
                rs.getString("vm_hostname"),
                MachineState.valueOf(rs.getString("machine_state")),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"),
                Boolean.parseBoolean(rs.getString("locked"))
        );
    }
}
