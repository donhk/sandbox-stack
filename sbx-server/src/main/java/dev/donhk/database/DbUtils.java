package dev.donhk.database;

import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.MachineState;
import dev.donhk.rest.NetworkType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUtils {
    public static MachineRow resultSetToMachineRow(ResultSet rs) throws SQLException {
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
