package dev.donhk.client;

import dev.donhk.exception.SandboxException;
import dev.donhk.rest.network.CreatePortForwardRuleResponse;
import dev.donhk.rest.operations.vm.CreateMachineRequest;
import dev.donhk.rest.types.MachineState;
import dev.donhk.rest.types.Network;
import dev.donhk.rest.types.NetworkType;
import dev.donhk.retry.MachinePoller;
import dev.donhk.ssh.SshClient;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract base class for sandbox test scenarios.
 *
 * <p>Subclasses must implement {@link #setup()}, {@link #test()}, and
 * {@link #resultVerification()}. The framework calls them in order via
 * {@link TestExecutor}, surrounding them with automatic VM provisioning
 * and cleanup.
 *
 * <p>Minimal subclass example:
 * <pre>{@code
 * public class MyTest extends TestEngine {
 *     public MyTest(SandboxClient client) { super(client); }
 *
 *     public void setup() {
 *         seedName      = "ubuntu22";
 *         snapshotName  = "snap-001";
 *         sshUser       = "user";
 *         sshPass       = "pass";
 *     }
 *
 *     public void test() throws Exception {
 *         String hostname = ssh.execute("hostname");
 *         System.out.println("VM hostname: " + hostname);
 *     }
 *
 *     public void resultVerification() throws Exception {
 *         // assert outcomes here
 *     }
 * }
 * }</pre>
 */
public abstract class TestEngine {

    protected final SandboxClient client;

    // ── Configured by user in setup() ────────────────────────────────────────

    /** Seed (base VM) name to clone from. Required. */
    protected String seedName;

    /** Snapshot of the seed to clone. Required. */
    protected String snapshotName;

    /** SSH username for the VM. Required. */
    protected String sshUser;

    /** SSH password for the VM. Required. */
    protected String sshPass;

    /**
     * NAT network name. Leave null to auto-generate one.
     * Must match {@code \w+_\w+} (e.g., {@code mytest_net}).
     */
    protected String networkName;

    /** Network type. Defaults to NAT. */
    protected NetworkType networkType = NetworkType.NAT;

    /** Set to > 0 to create and attach shared storage. Size in bytes per disk. */
    protected long storageSizeBytes = 0;

    /** Number of disks to create (used when storageSizeBytes > 0). */
    protected int numDisks = 0;

    /** How long to wait for the VM's SSH to become reachable. */
    protected Duration sshReadyTimeout = Duration.ofMinutes(8);

    // ── Set by the framework after provisioning ───────────────────────────────

    /** UUID assigned to this VM instance. */
    protected String uuid;

    /** Host-side port forwarded to the VM's SSH port (22). */
    protected int sshHostPort = -1;

    /** Ready-to-use SSH client connected to the VM. */
    protected SshClient ssh;

    // ─────────────────────────────────────────────────────────────────────────

    protected TestEngine(SandboxClient client) {
        this.client = client;
    }

    // ── Abstract lifecycle ────────────────────────────────────────────────────

    /**
     * Called first. Set {@code seedName}, {@code snapshotName}, {@code sshUser},
     * {@code sshPass}, and any optional fields here.
     */
    public abstract void setup();

    /**
     * Called after the VM is running and SSH is reachable.
     * Use {@link #ssh} to run commands on the VM.
     */
    public abstract void test() throws Exception;

    /**
     * Called after {@link #test()}. Assert outcomes and record results.
     */
    public abstract void resultVerification() throws Exception;

    // ── Optional override ─────────────────────────────────────────────────────

    /**
     * Called between {@code setupEnvironment()} and {@link #test()}.
     * Override to do additional VM setup (install packages, copy files, etc.)
     * after the environment is ready.
     */
    public void testEnvSetup() throws Exception {}

    // ── Framework internals (called by TestExecutor) ──────────────────────────

    protected void setupEnvironment() throws Exception {
        validateConfig();

        uuid = generateId();
        if (networkName == null) {
            networkName = uuid + "_net";
        }

        Logger.info("Provisioning VM uuid={} seed={} snapshot={}", uuid, seedName, snapshotName);

        // 1. Clone VM
        Optional<Network> network = Optional.of(new Network(networkType, networkName));
        CreateMachineRequest createReq = new CreateMachineRequest(
                uuid, uuid, seedName, snapshotName, network, List.of());
        client.createMachine(createReq);
        Logger.info("Machine cloned: {}", uuid);

        // 2. Create and attach shared storage if requested
        if (storageSizeBytes > 0 && numDisks > 0) {
            client.createStorageUnits(uuid, storageSizeBytes, numDisks);
            Logger.info("Storage attached: {} x {} bytes", numDisks, storageSizeBytes);
        }

        // 3. Start VM
        client.startMachine(uuid);
        Logger.info("Machine started: {}", uuid);

        // 4. Create SSH port-forward rule (host:hostPort → VM:22)
        CreatePortForwardRuleResponse portResp = client.createPortForwardRule(uuid, 22, "ssh");
        sshHostPort = portResp.hostPort();
        Logger.info("SSH port forward: {}:{} -> VM:22", client.getHost(), sshHostPort);

        // 5. Wait for SSH to become reachable
        ssh = buildSshClient(client.getHost(), sshHostPort, sshUser, sshPass);
        ssh.waitForReady(sshReadyTimeout);
        Logger.info("VM is ready: {}", uuid);
    }

    protected void cleanup() {
        Logger.info("Cleaning up VM: {}", uuid);
        if (ssh != null) {
            ssh.close();
            ssh = null;
        }
        if (uuid != null) {
            try {
                client.deleteMachine(uuid);
                Logger.info("VM deleted: {}", uuid);
            } catch (Exception e) {
                Logger.warn("Failed to delete VM {}: {}", uuid, e.getMessage());
            }
        }
    }

    public int getSshHostPort() {
        return sshHostPort;
    }

    // ── Hooks (overridable for testing) ───────────────────────────────────────

    protected SshClient buildSshClient(String host, int port, String user, String pass) {
        return new SshClient(host, port, user, pass);
    }

    protected MachinePoller buildPoller(String machineUuid) {
        return new MachinePoller(client, machineUuid);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateConfig() {
        if (seedName == null || seedName.isBlank()) {
            throw new SandboxException("seedName must be set in setup()");
        }
        if (snapshotName == null || snapshotName.isBlank()) {
            throw new SandboxException("snapshotName must be set in setup()");
        }
        if (sshUser == null || sshUser.isBlank()) {
            throw new SandboxException("sshUser must be set in setup()");
        }
        if (sshPass == null) {
            throw new SandboxException("sshPass must be set in setup()");
        }
    }

    private static String generateId() {
        return "sbx" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
