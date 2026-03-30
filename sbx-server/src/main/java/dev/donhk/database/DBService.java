// package and imports
package dev.donhk.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.config.Config;
import dev.donhk.pojos.*;
import dev.donhk.rest.types.*;

import java.util.List;

import java.sql.*;
import java.util.*;

public class DBService {

    private final HikariDataSource pool;
    private final Config config;

    public DBService(HikariDataSource pool, Config config) {
        this.pool = pool;
        this.config = config;
    }

    /**
     * Retrieves a machine and its associated port forwarding rule from the database.
     * <p>
     * This method performs a LEFT JOIN between the {@code machines} and {@code rules} tables
     * to fetch the machine metadata along with one associated rule (if any).
     * If the machine exists, it returns a populated {@link MachineRow} object; otherwise, it returns {@code null}.
     * </p>
     *
     * @param vmId the name of the machine to find
     * @return a {@link MachineRow} object containing the machine's data and one associated rule, or {@code null} if not found
     * @throws SQLException if a database access error occurs during the query
     */
    public Optional<MachineRow> findMachine(Connection connection, String vmId) throws SQLException {
        String sql = """
                SELECT id as uuid,
                       name,
                       seed_name,
                       snapshot,
                       network,
                       network_type,
                       vm_ip_address,
                       hostname,
                       vm_hostname,
                       machine_state,
                       created_at,
                       updated_at,
                       locked
                FROM virtual_machines
                where id=?
                ORDER BY created_at
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, vmId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(MachineRow.fromResultSet(rs));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<MachineRow> findMachine(String vmId) throws SQLException {
        try (Connection connection = pool.getConnection()) {
            return findMachine(connection, vmId);
        }
    }

    /**
     * Retrieves a list of all machines from the database, including their metadata and associated port forwarding rules.
     * <p>
     * This method performs a LEFT JOIN between the {@code machines} and {@code rules} tables to include rule data,
     * and returns a list of {@link MachineRow} objects sorted by their creation time in ascending order.
     * </p>
     *
     * @return a list of {@link MachineRow} objects containing machine details and rule information (if any)
     * @throws SQLException if a database access error occurs during the query
     */
    @SuppressWarnings("unused")
    public List<MachineRow> listAllVirtualMachines() throws SQLException {
        List<MachineRow> rows = new ArrayList<>();
        String sql = """
                SELECT id as uuid,
                       name,
                       seed_name,
                       snapshot,
                       network,
                       network_type,
                       vm_ip_address,
                       hostname,
                       vm_hostname,
                       machine_state,
                       created_at,
                       updated_at,
                       locked
                FROM virtual_machines
                ORDER BY created_at
                """;
        try (Connection connection = pool.getConnection();
             Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(MachineRow.fromResultSet(rs));
            }
        }
        return rows;
    }

    public List<VMPortRow> listVmPorts(String vmId) throws SQLException {
        List<VMPortRow> result = new LinkedList<>();
        String sql = """
                select vm_id as uuid,
                       name,
                       host_port,
                       vm_port
                from vm_ports
                where vm_id = ?
                """;

        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, vmId);
            try (ResultSet set = ps.executeQuery()) {
                while (set.next()) {
                    result.add(new VMPortRow(
                            set.getString("uuid"),
                            set.getString("name"),
                            set.getInt("host_port"),
                            set.getInt("vm_port")
                    ));
                }
            }
        }

        return result;
    }

    public List<StorageUnit> listStorageDisks(String vmId) throws SQLException {
        List<StorageUnit> result = new LinkedList<>();
        String sql = """
                select vm_id as uuid,
                       name,
                       size_bytes
                from vm_storage_units
                where vm_id = ?
                """;
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, vmId);
            try (ResultSet set = ps.executeQuery()) {
                while (set.next()) {
                    result.add(new StorageUnit(
                            set.getString("name"),
                            set.getString("size_bytes")
                    ));
                }
            }
        }
        return result;
    }

    public Optional<MachineRow> updateVmLockState(String vmId, boolean state) throws SQLException {
        String sql = """
                    UPDATE virtual_machines
                    SET locked = ?
                    WHERE id = ?
                """;
        try (Connection connection = pool.getConnection()) {
            try {
                connection.setAutoCommit(false); // BEGIN TRANSACTION

                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setBoolean(1, state);
                    ps.setString(2, vmId);
                    ps.executeUpdate();
                }

                Optional<MachineRow> result = this.findMachine(connection, vmId);

                connection.commit();
                return result;

            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<VMSnapshot> listSnapshots() throws SQLException {
        List<VMSnapshot> rows = new ArrayList<>();
        String sql = """
                SELECT  prefix,
                        machine_name,
                        vm_user,
                        vm_pass,
                        home,
                        snapshot_name,
                        snapshot_cpus,
                        snapshot_ram_mb,
                        snapshot_comments
                FROM vm_seeds
                ORDER BY prefix,snapshot_name
                """;
        try (Connection connection = pool.getConnection();
             Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(VMSnapshot.fromResultSet(rs));
            }
        }
        return rows;
    }

    public void upsertSeed(String prefix, String machineName, String user, String pass, String home,
                           String snapshotName, int cpus, int ramMb, String comments) throws SQLException {
        String sql = """
                MERGE INTO vm_seeds (prefix, machine_name, vm_user, vm_pass, home, snapshot_name, snapshot_cpus, snapshot_ram_mb, snapshot_comments)
                KEY (prefix, snapshot_name)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, prefix);
            ps.setString(2, machineName);
            ps.setString(3, user);
            ps.setString(4, pass);
            ps.setString(5, home);
            ps.setString(6, snapshotName);
            ps.setInt(7, cpus);
            ps.setInt(8, ramMb);
            ps.setString(9, comments);
            ps.executeUpdate();
        }
    }

    public List<ResourceRow> getLocalResources(String resource, int granularityMin, int daysBack, int limit) throws SQLException {
        List<ResourceRow> rows = new ArrayList<>();

        String sql = String.format(
                """
                        SELECT
                            TIMESTAMPADD(
                                MINUTE,
                                -MOD(DATEDIFF(MINUTE, TIMESTAMP '1970-01-01 00:00:00', created_at), %d),
                                created_at
                            ) AS dt_min,
                            AVG(usage) AS avg_usage
                        FROM resources_table
                        WHERE
                            resource = ? AND
                            created_at <= CURRENT_TIMESTAMP AND
                            created_at >= DATEADD('DAY', ?, CURRENT_DATE)
                        GROUP BY dt_min
                        ORDER BY dt_min DESC
                        limit ?;
                        """, granularityMin
        );

        try (Connection connection = pool.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, resource);
            stmt.setInt(2, daysBack);
            stmt.setInt(3, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(ResourceRow.fromResultSet(rs));
                }
            }
        }
        // Reverse result to keep ascending time order (since SQL returns DESC)
        Collections.reverse(rows);
        return rows;
    }

    public void insertMachine(String uuid, String name, String seedName, String snapshot,
                              String network, NetworkType networkType, String hostname, MachineState state) throws SQLException {
        String sql = """
                INSERT INTO virtual_machines
                    (id, name, seed_name, snapshot, network, network_type, hostname, machine_state, created_at, updated_at, locked)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE)
                """;
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, name);
            ps.setString(3, seedName);
            ps.setString(4, snapshot);
            ps.setString(5, network);
            ps.setString(6, networkType.name());
            ps.setString(7, hostname);
            ps.setString(8, state.name());
            ps.executeUpdate();
        }
    }

    public void deleteMachine(String uuid) throws SQLException {
        String selectSql = "SELECT name, snapshot, network, network_type FROM virtual_machines WHERE id = ?";
        String historySql = """
                INSERT INTO vms_history (deleted_at, name, snapshot, network, network_type)
                VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?)
                """;
        String deletePortsSql = "DELETE FROM vm_ports WHERE vm_id = ?";
        String deleteStorageSql = "DELETE FROM vm_storage_units WHERE vm_id = ?";
        String deleteSql = "DELETE FROM virtual_machines WHERE id = ?";

        try (Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String name = null, snapshot = null, network = null, networkType = null;
                try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                    ps.setString(1, uuid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            name = rs.getString("name");
                            snapshot = rs.getString("snapshot");
                            network = rs.getString("network");
                            networkType = rs.getString("network_type");
                        }
                    }
                }
                if (name != null) {
                    try (PreparedStatement ps = connection.prepareStatement(historySql)) {
                        ps.setString(1, name);
                        ps.setString(2, snapshot);
                        ps.setString(3, network);
                        ps.setString(4, networkType);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(deletePortsSql)) {
                    ps.setString(1, uuid);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(deleteStorageSql)) {
                    ps.setString(1, uuid);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
                    ps.setString(1, uuid);
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void updateMachineIp(String uuid, String ip) throws SQLException {
        String sql = "UPDATE virtual_machines SET vm_ip_address = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setString(2, uuid);
            ps.executeUpdate();
        }
    }

    public void updateMachineState(String uuid, MachineState state) throws SQLException {
        String sql = "UPDATE virtual_machines SET machine_state = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, state.name());
            ps.setString(2, uuid);
            ps.executeUpdate();
        }
    }

    public void updateMachineTimestamp(String uuid) throws SQLException {
        String sql = "UPDATE virtual_machines SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.executeUpdate();
        }
    }

    public void insertVmPort(String uuid, String name, int hostPort, int vmPort) throws SQLException {
        String sql = "INSERT INTO vm_ports (vm_id, name, host_port, vm_port) VALUES (?, ?, ?, ?)";
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, name);
            ps.setInt(3, hostPort);
            ps.setInt(4, vmPort);
            ps.executeUpdate();
        }
    }

    public Optional<VMPortRow> findVmPort(String uuid, int vmPort) throws SQLException {
        String sql = "SELECT vm_id as uuid, name, host_port, vm_port FROM vm_ports WHERE vm_id = ? AND vm_port = ?";
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setInt(2, vmPort);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new VMPortRow(
                            rs.getString("uuid"),
                            rs.getString("name"),
                            rs.getInt("host_port"),
                            rs.getInt("vm_port")
                    ));
                }
                return Optional.empty();
            }
        }
    }

    public void updateVmPortName(String uuid, int vmPort, String newRuleName) throws SQLException {
        String sql = "UPDATE vm_ports SET name = ? WHERE vm_id = ? AND vm_port = ?";
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newRuleName);
            ps.setString(2, uuid);
            ps.setInt(3, vmPort);
            ps.executeUpdate();
        }
    }

    public void insertStorageUnits(String uuid, List<String> diskPaths, long sizeBytes) throws SQLException {
        String sql = "INSERT INTO vm_storage_units (vm_id, name, size_bytes) VALUES (?, ?, ?)";
        try (Connection connection = pool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (String diskPath : diskPaths) {
                ps.setString(1, uuid);
                ps.setString(2, diskPath);
                ps.setLong(3, sizeBytes);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Integer> findUsedHostPorts() throws SQLException {
        List<Integer> ports = new ArrayList<>();
        String sql = "SELECT host_port FROM vm_ports";
        try (Connection connection = pool.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ports.add(rs.getInt("host_port"));
            }
        }
        return ports;
    }

    public VmHistoryRow getVmsHistory(int monthsBack, int limit) throws SQLException {
        String sql = """
                SELECT
                     FORMATDATETIME(deleted_at, 'MMMM yyyy') AS year_month,
                        COUNT(*) AS vm_count
                 FROM vms_history
                 WHERE deleted_at >= DATEADD('MONTH', ?, CURRENT_DATE)
                 GROUP BY FORMATDATETIME(deleted_at, 'MMMM yyyy')
                 ORDER BY MIN(deleted_at) DESC
                 LIMIT ?
                """;

        List<String> labels = new ArrayList<>();
        List<String> counts = new ArrayList<>();
        try (Connection connection = pool.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, monthsBack);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    labels.add(rs.getString("year_month"));
                    counts.add(rs.getString("vm_count"));
                }
            }
        }
        // Reverse result to keep ascending time order (since SQL returns DESC)
        Collections.reverse(labels);
        Collections.reverse(counts);
        return new VmHistoryRow(labels, counts);

    }

}
