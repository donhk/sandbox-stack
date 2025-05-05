// package and imports
package dev.donhk.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.config.Config;
import dev.donhk.pojos.*;
import dev.donhk.rest.types.ResourceRow;
import dev.donhk.rest.types.StorageUnit;
import dev.donhk.rest.types.VMSnapshot;

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

    public List<ResourceRow> getLocalResources(String resource, int granularity, int daysBack) throws SQLException {
        List<ResourceRow> rows = new ArrayList<>();
        String sql = """
                SELECT
                    FORMATDATETIME(
                        TIMESTAMPADD(
                            MINUTE,
                            -MOD(MINUTE(created_at), 10),
                            created_at
                        ),
                        'yyyy-MM-dd HH:mm'
                    ) AS dt_min,
                    AVG(usage) AS avg_usage
                FROM resources_table
                WHERE
                    resource = ? AND
                    created_at <= CURRENT_TIMESTAMP AND
                    created_at >= DATEADD('DAY', ?, CURRENT_DATE)
                GROUP BY
                    FORMATDATETIME(
                        TIMESTAMPADD(
                            MINUTE,
                            -MOD(MINUTE(created_at), 10),
                            created_at
                        ),
                        'yyyy-MM-dd HH:mm'
                    )
                ORDER BY dt_min ASC;
                """;
        try (Connection connection = pool.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, resource);
            stmt.setInt(2, daysBack);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(ResourceRow.fromResultSet(rs));
                }
            }
        }
        return rows;
    }

}
