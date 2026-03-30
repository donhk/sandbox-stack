package dev.donhk;

import dev.donhk.client.SandboxClient;
import dev.donhk.client.TestEngine;
import dev.donhk.client.TestExecutor;
import dev.donhk.rest.types.VMSnapshot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration smoke tests against a live sbx-server.
 *
 * <p>All tests are skipped automatically when the server is not reachable.
 * Run with a server at {@code SBX_URL} (default: {@code http://localhost:8008}).
 *
 * <p>The {@code @Tag("integration")} tests require VirtualBox and a configured
 * seed VM — they are separated so CI can run only smoke tests by default.
 */
class IntegrationSmokeTest {

    static final String SERVER_URL =
            System.getenv().getOrDefault("SBX_URL", "http://localhost:8008");

    static SandboxClient client;

    @BeforeAll
    static void checkServerReachable() {
        assumeTrue(isReachable(SERVER_URL),
                "Skipping integration tests — server not reachable at " + SERVER_URL);
        client = new SandboxClient(SERVER_URL);
    }

    // ── Basic connectivity ────────────────────────────────────────────────────

    @Test
    void ping_returns_pong() {
        assertDoesNotThrow(() -> client.ping());
    }

    @Test
    void listMachines_returns_list() {
        List<?> machines = client.listMachines();
        assertNotNull(machines);
    }

    @Test
    void listSeeds_returns_list() {
        List<VMSnapshot> seeds = client.listSeeds();
        assertNotNull(seeds);
    }

    // ── Seed scan (requires VirtualBox on the server host) ───────────────────

    @Test
    @Tag("vbox")
    void scanMachines_succeeds() {
        assumeTrue(isVBoxAvailable(), "Skipping — VirtualBox not available on server host");
        assertDoesNotThrow(() -> client.scanMachines());
    }

    @Test
    @Tag("vbox")
    void seeds_populated_after_scan() {
        assumeTrue(isVBoxAvailable(), "Skipping — VirtualBox not available on server host");
        client.scanMachines();
        List<VMSnapshot> seeds = client.listSeeds();
        assertNotNull(seeds);
    }

    // ── Full VM lifecycle (needs VBox + configured seed) ─────────────────────

    @Test
    @Tag("vbox")
    @Tag("integration")
    void full_lifecycle_creates_runs_and_destroys_vm() throws Exception {
        assumeTrue(isVBoxAvailable(), "Skipping — VirtualBox not available on server host");
        List<VMSnapshot> seeds = client.listSeeds();
        assumeTrue(!seeds.isEmpty(), "Skipping — no seeds available in the server");

        VMSnapshot seed = seeds.get(0);

        TestExecutor executor = new TestExecutor();
        LifecycleVerificationEngine engine = new LifecycleVerificationEngine(client, seed);
        executor.execute(engine);

        assertTrue(engine.testRan, "test() should have executed");
        assertTrue(engine.sshWorked, "SSH command should have succeeded");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean isVBoxAvailable() {
        try {
            // Probe the server: if /api/machines/scan returns non-500 VBox is present.
            // A 200 means VBox is up; anything else means it's not configured.
            HttpClient http = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/machines/scan"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isReachable(String url) {
        try {
            HttpClient http = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/api/ping"))
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Full-lifecycle test engine ────────────────────────────────────────────

    static class LifecycleVerificationEngine extends TestEngine {

        boolean testRan = false;
        boolean sshWorked = false;
        String hostnameOutput = null;

        private final VMSnapshot seed;

        LifecycleVerificationEngine(SandboxClient client, VMSnapshot seed) {
            super(client);
            this.seed = seed;
        }

        @Override
        public void setup() {
            seedName     = seed.prefix();
            snapshotName = seed.snapshot_name();
            sshUser      = seed.vm_user();
            sshPass      = seed.vm_pass();
            sshReadyTimeout = Duration.ofMinutes(10);
        }

        @Override
        public void test() throws Exception {
            testRan = true;
            hostnameOutput = ssh.execute("hostname");
            sshWorked = hostnameOutput != null && !hostnameOutput.isBlank();
        }

        @Override
        public void resultVerification() {
            assertTrue(sshWorked, "Expected a non-empty hostname from the VM");
            assertNotNull(hostnameOutput);
        }
    }
}
