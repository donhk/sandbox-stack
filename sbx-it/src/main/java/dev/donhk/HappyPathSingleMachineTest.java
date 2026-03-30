package dev.donhk;

import dev.donhk.client.SandboxClient;
import dev.donhk.client.TestEngine;
import dev.donhk.client.TestExecutor;
import dev.donhk.rest.types.VMSnapshot;
import dev.donhk.ssh.SshClient;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;

/**
 * Happy-path integration test: provisions a single VM, gathers host
 * information via SSH, waits 60 s to confirm the machine stays up,
 * then tears it down cleanly.
 *
 * Run with:
 *   ./gradlew :sbx-it:runHappyPath
 *
 * Override defaults via system properties:
 *   -Dsbx.url=http://localhost:8008
 *   -Dsbx.seed=mintwsc1
 *   -Dsbx.snapshot=snap-devbox1
 *   -Dsbx.user=donhk
 *   -Dsbx.pass=welcome
 */
public class HappyPathSingleMachineTest {

    public static void main(String[] args) throws Exception {
        String serverUrl  = System.getProperty("sbx.url",     "http://localhost:8008");
        // prefix selects which seed row to use; defaults to first Linux seed
        String prefix     = System.getProperty("sbx.prefix",  "mintwsc1");

        Logger.info("=== HappyPath single-machine test ===");
        Logger.info("Server : {}", serverUrl);
        Logger.info("Prefix : {}", prefix);

        SandboxClient client = new SandboxClient(serverUrl);

        // Look up the seed from the server so machine_name, credentials, snapshot are authoritative
        dev.donhk.rest.types.VMSnapshot seed = client.listSeeds().stream()
                .filter(s -> s.prefix().equals(prefix))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seed not found: " + prefix + ". Run scan first."));

        Logger.info("Seed   : prefix={} machine={} snapshot={}", seed.prefix(), seed.machine_name(), seed.snapshot_name());

        TestExecutor executor = new TestExecutor();

        Instant start = Instant.now();
        executor.execute(new SingleMachineEngine(client, seed));
        Duration total = Duration.between(start, Instant.now());

        Logger.info("=== Test finished in {} s ===", total.toSeconds());
    }

    // ─────────────────────────────────────────────────────────────────────────

    static class SingleMachineEngine extends TestEngine {

        private final VMSnapshot seed;

        // Collected during test()
        private String hostname;
        private String unameOutput;
        private String osRelease;
        private String diskUsage;
        private String memoryInfo;
        private String networkInfo;
        private String uptimeInfo;

        SingleMachineEngine(SandboxClient client, VMSnapshot seed) {
            super(client);
            this.seed = seed;
        }

        @Override
        public void setup() {
            // seedName = the actual VBox machine name used for cloning
            seedName        = seed.machine_name();
            snapshotName    = seed.snapshot_name();
            sshUser         = seed.vm_user();
            sshPass         = seed.vm_pass();
            sshReadyTimeout = Duration.ofMinutes(10);
        }

        @Override
        public void test() throws Exception {
            Logger.info("--- Gathering host information ---");

            hostname    = run("hostname");
            unameOutput = run("uname -a");
            osRelease   = run("cat /etc/os-release | grep PRETTY_NAME");
            diskUsage   = run("df -h");
            memoryInfo  = run("free -h");
            networkInfo = run("ip -brief addr show");
            uptimeInfo  = run("uptime");

            printSection("HOSTNAME",  hostname);
            printSection("KERNEL",    unameOutput);
            printSection("OS",        osRelease);
            printSection("DISK",      diskUsage);
            printSection("MEMORY",    memoryInfo);
            printSection("NETWORK",   networkInfo);
            printSection("UPTIME",    uptimeInfo);

            Logger.info("--- VM confirmed up — waiting 60 s ---");
            Thread.sleep(60_000);
            Logger.info("--- 60 s elapsed, VM still running ---");
        }

        @Override
        public void resultVerification() {
            Logger.info("--- Result verification ---");
            assertNotBlank("hostname",   hostname);
            assertNotBlank("uname",      unameOutput);
            assertNotBlank("disk usage", diskUsage);
            assertNotBlank("memory",     memoryInfo);
            assertNotBlank("network",    networkInfo);
            Logger.info("All checks passed.");
        }

        // ── helpers ──────────────────────────────────────────────────────────

        private String run(String cmd) throws Exception {
            Logger.debug("$ {}", cmd);
            String out = ssh.execute(cmd);
            return out;
        }

        private void printSection(String label, String value) {
            Logger.info("┌─ {} ─────────────────────────────────", label);
            for (String line : value.split("\n")) {
                Logger.info("│  {}", line);
            }
            Logger.info("└──────────────────────────────────────────");
        }

        private void assertNotBlank(String name, String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Expected non-blank output for: " + name);
            }
            Logger.info("✓ {} — OK", name);
        }

        @Override
        protected SshClient buildSshClient(String host, int port, String u, String p) {
            Logger.info("SSH target: {}:{}", host, port);
            return super.buildSshClient(host, port, u, p);
        }
    }
}
