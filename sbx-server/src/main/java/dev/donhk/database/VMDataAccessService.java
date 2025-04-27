// package and imports
package dev.donhk.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.donhk.config.Config;
import dev.donhk.pojos.*;
import dev.donhk.rest.types.StorageUnit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class VMDataAccessService {

    private final HikariDataSource pool;
    private final Config config;

    public VMDataAccessService(HikariDataSource pool, Config config) {
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
    public MachineRow findMachine(Connection connection, String vmId) throws SQLException {
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
                ORDER BY created_at asc
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, vmId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return MachineRow.fromResultSet(rs);
                }
                return null;
            }
        }
    }

    public MachineRow findMachine(String vmId) throws SQLException {
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
                ORDER BY created_at asc
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

    public MachineRow updateVmLockState(String vmId, boolean state) throws SQLException {
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

                MachineRow result = this.findMachine(connection, vmId);

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

    // old code

    /**
     * Removes a machine and its associated data from the database.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Finds the machine with the given name using {@link #findMachine(String)}.</li>
     *   <li>If the machine does not exist, it throws an {@link SQLException}.</li>
     *   <li>Inserts the machine's current data into a historical table using {@link #insertMachineHist(String)}.</li>
     *   <li>Deletes any rules associated with the machine using {@link #dropRule(String)}.</li>
     *   <li>Deletes the machine record itself using {@link #dropMachine(String)}.</li>
     * </ol>
     *
     * @param machineName the name of the machine to remove
     * @throws SQLException if the machine is not found or if a database error occurs during the removal process
     */
    public void removeMachine(String machineName) throws SQLException {
        MachineRow machineRow = findMachine(machineName);
        if (machineRow == null) throw new SQLException("No machine found: " + machineName);
        insertMachineHist(machineRow.name());
        dropRule(machineRow.uuid());
        dropMachine(machineRow.uuid());
    }

    /**
     * Returns the first available host port within the configured port range.
     * <p>
     * This method generates random port numbers within the range specified by
     * {@code config.sbxServiceLowPort} and {@code config.sbxServiceHighPort},
     * and checks whether the port is marked as FREE in the {@code HOSTPORT} table.
     * It continues generating and checking ports until a FREE port is found.
     * </p>
     *
     * @return an available port number that is marked as FREE in the database
     * @throws SQLException if a database access error occurs
     */
    public int getHostPort() throws SQLException {
        final Random random = new Random();
        final String sql = "select count(1) port from HOSTPORT where status=? and port=?";
        do {
            final int potentialFreePort = random.nextInt(this.config.sbxServiceHighPort - this.config.sbxServiceLowPort) + this.config.sbxServiceLowPort;
            try (PreparedStatement ps = pool.getConnection().prepareStatement(sql)) {
                ps.setString(1, HostPortStatus.FREE.name());
                ps.setInt(2, potentialFreePort);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("port") == 1) return potentialFreePort;
                }
            }
        } while (true);
    }

    /**
     * Inserts a new machine record into the {@code machines} table.
     * <p>
     * The method stores basic information about a virtual machine, including its name,
     * IPv4 address, current state, and the associated NAT network.
     * </p>
     *
     * @param name       the name of the machine
     * @param ipv4       the IPv4 address of the machine
     * @param state      the current state of the machine (e.g., RUNNING, STOPPED)
     * @param NATNetwork the name of the NAT network the machine is connected to
     * @throws SQLException if a database access error occurs during the insert operation
     */
    public void insertMachine(String name, String ipv4, String state, String NATNetwork) throws SQLException {
        String sql = "insert into machines (name,ipv4,state,network) values (?,?,?,?)";
        try (PreparedStatement ps = pool.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, ipv4);
            ps.setString(3, state);
            ps.setString(4, NATNetwork);
            ps.executeUpdate();
        }
    }

    /**
     * Updates the state and virtual machine (VM) identifier of a machine in the {@code machines} table.
     * <p>
     * This method locates the machine by its name and sets its {@code state} and {@code vm} fields
     * to the provided values.
     * </p>
     *
     * @param name  the name of the machine to update
     * @param state the new state of the machine (e.g., RUNNING, STOPPED)
     * @param vm    the new VM identifier or name associated with the machine
     * @throws SQLException if a database access error occurs during the update
     */
    public void updateVmMeta(String name, String state, String vm) throws SQLException {
        String sql = "update machines set state=?,vm=? where name=?";
        try (PreparedStatement ps = pool.getConnection().prepareStatement(sql)) {
            ps.setString(1, state);
            ps.setString(2, vm);
            ps.setString(3, name);
            ps.executeUpdate();
        }
    }

    /**
     * Updates the network information of a machine in the {@code machines} table.
     * <p>
     * This method sets the {@code network} field to the provided network name for the machine
     * identified by the given name.
     * </p>
     *
     * @param name        the name of the machine to update
     * @param networkName the new network name to associate with the machine
     * @throws SQLException if a database access error occurs during the update
     */
    public void updateVmNetworkInfo(String name, String networkName) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("update machines set network=? where name=?")) {
            ps.setString(1, networkName);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    /**
     * Updates the state of a machine in the {@code machines} table.
     * <p>
     * This method modifies the {@code state} field for the machine identified by the specified name.
     * </p>
     *
     * @param name  the name of the machine to update
     * @param state the new state to set for the machine (e.g., RUNNING, STOPPED)
     * @throws SQLException if a database access error occurs during the update
     */
    public void updateVmState(String name, String state) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("update machines set state=? where name=?")) {
            ps.setString(1, state);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    /**
     * Updates the IPv4 address and state of a machine in the {@code machines} table.
     * <p>
     * This method sets the {@code ipv4} and {@code state} fields for the machine identified by the given name.
     * </p>
     *
     * @param name  the name of the machine to update
     * @param ipv4  the new IPv4 address to assign to the machine
     * @param state the new state to set for the machine (e.g., RUNNING, STOPPED)
     * @throws SQLException if a database access error occurs during the update
     */
    public void updateMachine(String name, String ipv4, String state) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("update machines set ipv4=?,state=? where name=?")) {
            ps.setString(1, ipv4);
            ps.setString(2, state);
            ps.setString(3, name);
            ps.executeUpdate();
        }
    }

    /**
     * Inserts a new port forwarding rule for a machine into the {@code rules} table.
     * <p>
     * This method first marks the specified host port as {@code BUSY} by calling {@link #updatePort(int, HostPortStatus)}.
     * Then it inserts a new rule linking the machine name, rule description, host port, and guest port.
     * </p>
     *
     * @param machineName the name of the machine to associate with the rule
     * @param ruleName    the name or description of the rule (e.g., "ssh", "http")
     * @param hostPort    the port exposed on the host machine
     * @param guestPort   the port on the guest (VM) that maps to the host port
     * @throws SQLException if a database access error occurs during the insert or port update
     */
    public void insertRule(String machineName, String ruleName, int hostPort, int guestPort) throws SQLException {
        updatePort(hostPort, HostPortStatus.BUSY);
        try (PreparedStatement ps = pool.getConnection().prepareStatement("insert into rules (name,rule_name,hostport,guestport) values (?,?,?,?)")) {
            ps.setString(1, machineName);
            ps.setString(2, ruleName);
            ps.setInt(3, hostPort);
            ps.setInt(4, guestPort);
            ps.executeUpdate();
        }
    }

    /**
     * Inserts a new NAT network entry into the {@code nats} table.
     * <p>
     * This method stores the provided network name as a new record in the NAT networks table.
     * </p>
     *
     * @param name the name of the NAT network to insert
     * @throws SQLException if a database access error occurs during the insert
     */
    public void insertNATNetwork(String name) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("insert into nats (network) values (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    /**
     * Archives the current state of a machine and its associated port forwarding rules into the {@code machines_hist} table.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Retrieves all port forwarding rules for the specified machine and formats them into a single string.</li>
     *   <li>Fetches the current machine metadata including name, IP, state, VM name, creation time, and network.</li>
     *   <li>Inserts a historical record into the {@code machines_hist} table, capturing the machine's state and its rules.</li>
     * </ol>
     *
     * @param machineName the name of the machine to archive
     * @throws SQLException if a database access error occurs during any of the retrieval or insert operations
     */
    private void insertMachineHist(String machineName) throws SQLException {
        StringBuilder sbRules = new StringBuilder();
        try (PreparedStatement psr = pool.getConnection().prepareStatement("select rule_name, hostport, guestport from rules where name = ?")) {
            psr.setString(1, machineName);
            try (ResultSet set = psr.executeQuery()) {
                while (set.next()) {
                    sbRules.append("[").append(set.getString("rule_name")).append("|")
                            .append(set.getString("hostport")).append("|")
                            .append(set.getString("guestport")).append("]");
                }
            }
        }

        String name = "", ipv4 = "", state = "", vm = "", created = "", network = "";
        try (PreparedStatement psm = pool.getConnection().prepareStatement("select name, ipv4, state, vm, created, network from machines where name = ?")) {
            psm.setString(1, machineName);
            try (ResultSet mSet = psm.executeQuery()) {
                if (mSet.next()) {
                    name = mSet.getString("name");
                    ipv4 = mSet.getString("ipv4");
                    state = mSet.getString("state");
                    vm = mSet.getString("vm");
                    created = mSet.getString("created");
                    network = mSet.getString("network");
                }
            }
        }

        try (PreparedStatement ps = pool.getConnection().prepareStatement("insert into machines_hist(name,ipv4,network,rules,created,vm,state) values (?,?,?,?,?,?,?)")) {
            ps.setString(1, name);
            ps.setString(2, ipv4);
            ps.setString(3, network);
            ps.setString(4, sbRules.toString());
            ps.setString(5, created);
            ps.setString(6, vm);
            ps.setString(7, state);
            ps.executeUpdate();
        }
    }

    /**
     * Checks whether a machine with the given name exists in the {@code machines} table.
     * <p>
     * Executes a {@code SELECT COUNT(1)} query to determine if a machine with the specified name is present.
     * If an exception occurs during the query, it is silently ignored and {@code false} is returned.
     * </p>
     *
     * @param machineName the name of the machine to check
     * @return {@code true} if the machine exists; {@code false} otherwise or if an error occurs
     */
    public boolean machineExists(String machineName) {
        int count = 0;
        String sql = "SELECT count(1) total FROM MACHINES where name=?";
        try (PreparedStatement ps = pool.getConnection().prepareStatement(sql)) {
            ps.setString(1, machineName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) count = rs.getInt("total");
            }
        } catch (Exception ignored) {
        }
        return count > 0;
    }

    /**
     * Deletes all port forwarding rules associated with the specified machine from the {@code rules} table.
     * <p>
     * This method removes any entries in the {@code rules} table where the {@code name} matches the provided machine name.
     * </p>
     *
     * @param machineName the name of the machine whose rules should be deleted
     * @throws SQLException if a database access error occurs during the deletion
     */
    public void dropRule(String machineName) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("delete from rules where name=?")) {
            ps.setString(1, machineName);
            ps.executeUpdate();
        }
    }

    /**
     * Retrieves all port forwarding rules associated with the specified machine from the {@code rules} table.
     * <p>
     * Each rule includes the rule name, host port, and guest port, and is returned as a {@link Rule} object.
     * </p>
     *
     * @param machineName the name of the machine whose rules are to be fetched
     * @return a list of {@link Rule} objects representing the machine's port forwarding rules
     * @throws SQLException if a database access error occurs during the query
     */
    public List<Rule> getRules(String machineName) throws SQLException {
        List<Rule> result = new LinkedList<>();
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select rule_name,hostport,guestport from rules where name = ?")) {
            ps.setString(1, machineName);
            try (ResultSet set = ps.executeQuery()) {
                while (set.next()) {
                    result.add(new Rule(set.getString("rule_name"), set.getInt("hostport"), set.getInt("guestport")));
                }
            }
        }
        return result;
    }

    /**
     * Retrieves the SSH port forwarding rule for the specified machine from the {@code rules} table.
     * <p>
     * This method looks for a rule whose name starts with {@code "ssh"} and is associated with the given machine name.
     * If such a rule exists, it returns it as a {@link Rule} object; otherwise, it returns {@code null}.
     * </p>
     *
     * @param machineName the name of the machine to search for an SSH rule
     * @return the {@link Rule} object representing the SSH rule, or {@code null} if not found
     * @throws SQLException if a database access error occurs during the query
     */
    public Rule geSSHRule(String machineName) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select rule_name,hostport,guestport from rules where name = ? and rule_name like 'ssh%'")) {
            ps.setString(1, machineName);
            try (ResultSet set = ps.executeQuery()) {
                if (set.next()) {
                    return new Rule(set.getString("rule_name"), set.getInt("hostport"), set.getInt("guestport"));
                }
            }
        }
        return null;
    }

    /**
     * Deletes a machine record from the {@code machines} table based on the given machine name.
     * <p>
     * This method removes the machine entry that matches the specified name.
     * </p>
     *
     * @param machineName the name of the machine to delete
     * @throws SQLException if a database access error occurs during the deletion
     */
    private void dropMachine(String machineName) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("delete from machines where name=?")) {
            ps.setString(1, machineName);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a NAT network entry from the {@code nats} table based on the given network name.
     * <p>
     * This method removes the NAT network record that matches the specified name.
     * </p>
     *
     * @param NATNetwork the name of the NAT network to delete
     * @throws SQLException if a database access error occurs during the deletion
     */
    public void dropNATNetwork(String NATNetwork) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("delete from nats where network=?")) {
            ps.setString(1, NATNetwork);
            ps.executeUpdate();
        }
    }

    /**
     * Updates the status of a specific port in the {@code hostport} table.
     * <p>
     * This method sets the {@code status} field of the given port to the specified {@link HostPortStatus}.
     * </p>
     *
     * @param port      the host port number to update
     * @param newStatus the new status to assign to the port (e.g., FREE, BUSY)
     * @throws SQLException if a database access error occurs during the update
     */
    public void updatePort(int port, HostPortStatus newStatus) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("update hostport set status=? where port=?")) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, port);
            ps.executeUpdate();
        }
    }

    /**
     * Updates a port forwarding rule for a machine by deleting the old rule and inserting a new one.
     * <p>
     * This method first removes the existing rule identified by {@code oldRuleName} from the {@code rules} table,
     * then adds a new rule with the specified parameters using {@link #insertRule(String, String, int, int)}.
     * </p>
     *
     * @param machineName the name of the machine the rule is associated with
     * @param oldRuleName the name of the rule to be replaced
     * @param newRuleName the name of the new rule to insert
     * @param hostPort    the new host port for the rule
     * @param guestPort   the new guest port for the rule
     * @throws SQLException if a database access error occurs during deletion or insertion
     */
    public void updateRule(String machineName, String oldRuleName, String newRuleName, int hostPort, int guestPort) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("delete from rules where rule_name=?")) {
            ps.setString(1, oldRuleName);
            ps.executeUpdate();
        }
        insertRule(machineName, newRuleName, hostPort, guestPort);
    }

    /**
     * Replaces all existing entries in the {@code machines_meta} table with a new list of virtual machine metadata.
     * <p>
     * This method performs the following steps within a transaction:
     * <ol>
     *   <li>Disables auto-commit to begin a manual transaction.</li>
     *   <li>Truncates the {@code machines_meta} table to remove all existing records.</li>
     *   <li>Inserts the provided list of {@link MachineMeta} objects using batch processing for efficiency.</li>
     *   <li>Commits the transaction and restores the auto-commit setting.</li>
     * </ol>
     *
     * @param machines the list of {@link MachineMeta} objects to insert into the table
     * @throws SQLException if a database access error occurs during any step of the process
     */
    public void updateMachinesMeta(List<MachineMeta> machines) throws SQLException {
        pool.getConnection().setAutoCommit(false);
        try (PreparedStatement truncate = pool.getConnection().prepareStatement("truncate table machines_meta")) {
            truncate.executeUpdate();
        }
        try (PreparedStatement ps = pool.getConnection().prepareStatement("insert into machines_meta(machine_prefix,machine_name,snapshot_name,cpu_count,memory,user,password,home,comments) values(?,?,?,?,?,?,?,?,?)")) {
            for (MachineMeta meta : machines) {
                ps.setString(1, meta.machinePrefix);
                ps.setString(2, meta.machineName);
                ps.setString(3, meta.snapshotName);
                ps.setString(4, meta.cpuCount);
                ps.setString(5, meta.memorySize);
                ps.setString(6, meta.user);
                ps.setString(7, meta.password);
                ps.setString(8, meta.home);
                ps.setString(9, meta.comments);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        pool.getConnection().commit();
        pool.getConnection().setAutoCommit(true);
    }

    /**
     * Updates the {@code meta_digest} table with new metadata information.
     * <p>
     * This method first clears all existing records from the {@code meta_digest} table,
     * then inserts a new record containing the provided digest and content values.
     * </p>
     *
     * @param digest  the digest string representing the current state of the metadata
     * @param content the actual content associated with the digest
     * @throws SQLException if a database access error occurs during the truncate or insert operations
     */
    public void updateMetaInfoFile(String digest, String content) throws SQLException {
        try (PreparedStatement truncate = pool.getConnection().prepareStatement("truncate table meta_digest")) {
            truncate.executeUpdate();
        }
        try (PreparedStatement insert = pool.getConnection().prepareStatement("insert into meta_digest(digest,content) values(?,?)")) {
            insert.setString(1, digest);
            insert.setString(2, content);
            insert.executeUpdate();
        }
    }

    /**
     * Checks whether a specific combination of machine name and snapshot name exists in the {@code machines_meta} table.
     * <p>
     * Executes a {@code SELECT COUNT(1)} query to verify the presence of a record matching both the machine name
     * and the snapshot name.
     * </p>
     *
     * @param machineName the name of the machine to check
     * @param snapshot    the name of the snapshot to check
     * @return {@code true} if the combination exists; {@code false} otherwise
     * @throws SQLException if a database access error occurs during the query
     */
    public boolean machineAndSnapExist(String machineName, String snapshot) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select count(1) total from machines_meta where machine_name=? and snapshot_name=?")) {
            ps.setString(1, machineName);
            ps.setString(2, snapshot);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() && r.getInt("total") > 0;
            }
        }
    }

    /**
     * Retrieves the current metadata digest and its associated content from the {@code meta_digest} table.
     * <p>
     * This method executes a query to fetch the digest and content values. If a record exists, it returns them
     * as a single-entry map; otherwise, it returns an empty map.
     * </p>
     *
     * @return a map containing the digest as the key and the content as the value, or an empty map if no record is found
     * @throws SQLException if a database access error occurs during the query
     */
    public Map<String, String> getMetaDigestInfo() throws SQLException {
        Map<String, String> m = new HashMap<>();
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select digest, content from meta_digest");
             ResultSet r = ps.executeQuery()) {
            if (r.next()) {
                m.put(r.getString("digest"), r.getString("content"));
            }
        }
        return m;
    }

    /**
     * Retrieves the full metadata digest record from the {@code meta_digest} table.
     * <p>
     * This method queries the table for the digest, content, and creation timestamp.
     * If a record is found, it returns a {@link DigestRow} object populated with the data;
     * otherwise, it returns {@code null}.
     * </p>
     *
     * @return a {@link DigestRow} containing the digest information, or {@code null} if no record exists
     * @throws SQLException if a database access error occurs during the query
     */
    public DigestRow getDigestRow() throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select digest, content, created from meta_digest");
             ResultSet r = ps.executeQuery()) {
            if (r.next()) {
                return new DigestRow(r.getString("digest"), r.getString("created"), r.getString("content"));
            }
        }
        return null;
    }

    /**
     * Inserts a new operation entry into the {@code history} table.
     * <p>
     * This method logs the specified operation as a new record in the history table for auditing or tracking purposes.
     * </p>
     *
     * @param operation the description of the operation to record
     * @throws SQLException if a database access error occurs during the insert
     */
    public void registerOperation(String operation) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("insert into history(operation) values(?)")) {
            ps.setString(1, operation);
            ps.executeUpdate();
        }
    }

    /**
     * Retrieves all virtual machine metadata entries from the {@code machines_meta} table.
     * <p>
     * Each row in the table is mapped to a {@link MachineMeta} object containing details such as the machine name,
     * snapshot, CPU count, memory, user credentials, and additional comments.
     * </p>
     *
     * @return a list of {@link MachineMeta} objects representing all metadata entries
     * @throws SQLException if a database access error occurs during the query
     */
    public List<MachineMeta> getMachinesMetaInfo() throws SQLException {
        List<MachineMeta> meta = new ArrayList<>();
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select machine_prefix,machine_name,snapshot_name,cpu_count,memory,user,password,home,comments from machines_meta");
             ResultSet r = ps.executeQuery()) {
            while (r.next()) {
                meta.add(new MachineMeta(
                        r.getString("machine_prefix"),
                        r.getString("machine_name"),
                        r.getString("snapshot_name"),
                        r.getString("cpu_count"),
                        r.getString("memory"),
                        r.getString("user"),
                        r.getString("password"),
                        r.getString("home"),
                        r.getString("comments")
                ));
            }
        }
        return meta;
    }

    /**
     * Retrieves a preview of the most recent historical machine records from the {@code machines_hist} table.
     * <p>
     * This method fetches up to 30 records ordered by the {@code destroyed} timestamp in descending order.
     * Each record is mapped to a {@link MachineHistRow} containing historical machine data including network,
     * rules, creation and destruction timestamps, and state.
     * </p>
     *
     * @return a list of up to 30 {@link MachineHistRow} objects representing the most recently destroyed machines
     * @throws SQLException if a database access error occurs during the query
     */
    public List<MachineHistRow> getMachinesHistPreview() throws SQLException {
        List<MachineHistRow> meta = new ArrayList<>();
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select name,ipv4,network,rules,created,vm,state,destroyed from machines_hist order by destroyed desc limit 30");
             ResultSet r = ps.executeQuery()) {
            while (r.next()) {
                meta.add(new MachineHistRow(
                        r.getString("name"),
                        r.getString("ipv4"),
                        r.getString("network"),
                        r.getString("rules"),
                        r.getString("created"),
                        r.getString("vm"),
                        r.getString("state"),
                        r.getString("destroyed")
                ));
            }
        }
        return meta;
    }

    /**
     * Returns the total number of historical machine records stored in the {@code machines_hist} table.
     * <p>
     * Executes a {@code COUNT(1)} query to determine the number of entries in the table.
     * </p>
     *
     * @return the total count of records in {@code machines_hist}
     * @throws SQLException if a database access error occurs during the query
     */
    public int getTotalMachinesHist() throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select count(1) total from machines_hist");
             ResultSet r = ps.executeQuery()) {
            return r.next() ? r.getInt("total") : 0;
        }
    }

    /**
     * Returns the total number of operations recorded in the {@code history} table.
     * <p>
     * Executes a {@code COUNT(1)} query to determine how many update operations have been logged.
     * </p>
     *
     * @return the total number of update records in {@code history}
     * @throws SQLException if a database access error occurs during the query
     */
    public int getTotalUpdatesServed() throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select count(1) total from history");
             ResultSet r = ps.executeQuery()) {
            return r.next() ? r.getInt("total") : 0;
        }
    }

    /**
     * Retrieves a list of all active machines along with their metadata and the count of associated port forwarding rules.
     * <p>
     * This method performs a join between the {@code machines} and {@code rules} tables,
     * grouping by machine name and counting the number of rules for each machine.
     * The results are ordered by the machine creation time in descending order.
     * </p>
     *
     * @return a list of {@link ActiveMachineRow} objects representing active machines and their rule counts
     * @throws SQLException if a database access error occurs during the query
     */
    public List<ActiveMachineRow> getActiveMachines() throws SQLException {
        List<ActiveMachineRow> meta = new ArrayList<>();
        final String sql = "select m.name,ipv4,state,network,vm,created,count(1) rules from machines m join rules r on (m.name=r.name) group by m.name order by created desc";
        try (PreparedStatement ps = pool.getConnection().prepareStatement(sql); ResultSet r = ps.executeQuery()) {
            while (r.next()) {
                meta.add(new ActiveMachineRow(
                        r.getString("name"),
                        r.getString("ipv4"),
                        r.getString("state"),
                        r.getString("vm"),
                        r.getString("created"),
                        r.getString("network"),
                        r.getString("rules")
                ));
            }
        }
        return meta;
    }

    /**
     * Retrieves the current status of a specific port from the {@code hostport} table.
     * <p>
     * Executes a query to fetch the {@code status} value for the given port number.
     * </p>
     *
     * @param port the port number to check
     * @return the status of the port (e.g., FREE, BUSY), or {@code null} if the port is not found
     * @throws SQLException if a database access error occurs during the query
     */
    public String getPortStatus(int port) throws SQLException {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select status from hostport where port=?")) {
            ps.setInt(1, port);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? r.getString("status") : null;
            }
        }
    }

    /**
     * Updates the {@code updated} timestamp of a machine in the {@code machines} table to the current time.
     * <p>
     * This method is used to register a heartbeat or activity signal for the specified virtual machine.
     * If the VM name is {@code null}, the method returns immediately.
     * Any exceptions during the update are silently ignored.
     * </p>
     *
     * @param vm the name of the virtual machine to update
     */
    public void pollMachine(String vm) {
        if (vm == null) return;
        try (PreparedStatement ps = pool.getConnection().prepareStatement("update machines set updated = ? where name=?")) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, vm);
            ps.execute();
        } catch (Exception ignored) {
        }
    }

    /**
     * Retrieves the last updated (poll) timestamp for a machine from the {@code machines} table.
     * <p>
     * This method returns the value of the {@code updated} field for the specified machine name,
     * which typically represents the last heartbeat or activity time.
     * If an error occurs or the machine is not found, {@code null} is returned.
     * </p>
     *
     * @param name the name of the machine to query
     * @return the {@link Timestamp} of the last update, or {@code null} if not found or an error occurs
     */
    public Timestamp getMachinePoll(String name) {
        try (PreparedStatement ps = pool.getConnection().prepareStatement("select updated from machines where name=?")) {
            ps.setString(1, name);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? r.getTimestamp("updated") : null;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

}
