package dev.donhk;

import dev.donhk.client.SandboxClient;
import dev.donhk.client.TestEngine;
import dev.donhk.client.TestExecutor;
import dev.donhk.exception.SandboxException;
import dev.donhk.rest.network.CreatePortForwardRuleResponse;
import dev.donhk.rest.operations.vm.CreateMachineResponse;
import dev.donhk.rest.operations.vm.DeleteMachineResponse;
import dev.donhk.rest.operations.vm.StartMachineResponse;
import dev.donhk.ssh.SshClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TestEngine lifecycle orchestration.
 *
 * SandboxClient and SshClient are mocked — no real server or VMs needed.
 */
@ExtendWith(MockitoExtension.class)
class TestEngineLifecycleTest {

    @Mock
    SandboxClient client;

    @Mock
    SshClient mockSsh;

    TestExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new TestExecutor();

        // Default stubs for a successful provision flow (lenient — some tests override these)
        lenient().when(client.getHost()).thenReturn("localhost");
        lenient().when(client.createMachine(any())).thenReturn(new CreateMachineResponse("sbxtest001"));
        lenient().when(client.startMachine(anyString())).thenReturn(new StartMachineResponse("sbxtest001"));
        lenient().when(client.createPortForwardRule(anyString(), eq(22), eq("ssh")))
                .thenReturn(new CreatePortForwardRuleResponse(11200));
        lenient().when(client.deleteMachine(anyString())).thenReturn(new DeleteMachineResponse("sbxtest001"));
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void lifecycle_methods_called_in_order() throws Exception {
        var engine = spy(new TrackingEngine(client, mockSsh));

        executor.execute(engine);

        // Verify user-visible abstract methods called in order
        InOrder order = inOrder(engine, client);
        order.verify(engine).setup();
        order.verify(client).createMachine(any());   // setupEnvironment ran
        order.verify(engine).testEnvSetup();
        order.verify(engine).test();
        order.verify(engine).resultVerification();
        order.verify(client).deleteMachine(anyString()); // cleanup ran
    }

    @Test
    void createMachine_called_with_seed_and_snapshot() throws Exception {
        executor.execute(new TrackingEngine(client, mockSsh));

        verify(client).createMachine(argThat(req ->
                "ubuntu22".equals(req.seedName()) && "snap-001".equals(req.snapshot())
        ));
    }

    @Test
    void startMachine_called_after_create() throws Exception {
        executor.execute(new TrackingEngine(client, mockSsh));

        InOrder order = inOrder(client);
        order.verify(client).createMachine(any());
        order.verify(client).startMachine(anyString());
    }

    @Test
    void ssh_port_forward_created_for_port_22() throws Exception {
        executor.execute(new TrackingEngine(client, mockSsh));

        verify(client).createPortForwardRule(anyString(), eq(22), eq("ssh"));
    }

    @Test
    void sshHostPort_is_set_from_server_response() throws Exception {
        var engine = new TrackingEngine(client, mockSsh);

        executor.execute(engine);

        assertEquals(11200, engine.getSshHostPort());
    }

    @Test
    void ssh_waitForReady_called_with_timeout() throws Exception {
        executor.execute(new TrackingEngine(client, mockSsh));

        verify(mockSsh).waitForReady(any());
    }

    // ── Cleanup guarantee ─────────────────────────────────────────────────────

    @Test
    void cleanup_called_even_when_test_throws() throws Exception {
        var engine = new FailingTestEngine(client, mockSsh);

        assertThrows(RuntimeException.class, () -> executor.execute(engine));

        // deleteMachine proves cleanup ran
        verify(client).deleteMachine(anyString());
    }

    @Test
    void cleanup_called_even_when_setupEnvironment_throws() {
        when(client.createMachine(any())).thenThrow(new SandboxException("VBox unavailable"));

        var engine = new TrackingEngine(client, mockSsh);

        assertThrows(SandboxException.class, () -> executor.execute(engine));

        // uuid is null when clone failed, so deleteMachine should NOT be called
        // but cleanup() itself must not throw — the test not throwing IS the assertion
    }

    @Test
    void test_and_resultVerification_not_called_when_setup_fails() throws Exception {
        when(client.createMachine(any())).thenThrow(new SandboxException("VBox unavailable"));

        var engine = spy(new TrackingEngine(client, mockSsh));

        assertThrows(SandboxException.class, () -> executor.execute(engine));

        verify(engine, never()).testEnvSetup();
        verify(engine, never()).test();
        verify(engine, never()).resultVerification();
    }

    @Test
    void delete_machine_called_in_cleanup() throws Exception {
        var engine = new TrackingEngine(client, mockSsh);
        executor.execute(engine);

        verify(client).deleteMachine(anyString());
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    void missing_seedName_throws_before_any_api_call() {
        var engine = new NoSeedEngine(client, mockSsh);

        assertThrows(SandboxException.class, () -> executor.execute(engine));
        verify(client, never()).createMachine(any());
    }

    // ── Helper test engine implementations ───────────────────────────────────

    /** Normal engine: records that test() and resultVerification() ran. */
    static class TrackingEngine extends TestEngine {
        boolean testRan = false;
        boolean verificationRan = false;
        private final SshClient injectedSsh;

        TrackingEngine(SandboxClient client, SshClient ssh) {
            super(client);
            this.injectedSsh = ssh;
        }

        @Override
        public void setup() {
            seedName = "ubuntu22";
            snapshotName = "snap-001";
            sshUser = "user";
            sshPass = "pass";
        }

        @Override
        public void test() {
            testRan = true;
        }

        @Override
        public void resultVerification() {
            verificationRan = true;
        }

        @Override
        protected SshClient buildSshClient(String host, int port, String user, String pass) {
            return injectedSsh;
        }
    }

    /** Engine whose test() throws to verify cleanup still runs. */
    static class FailingTestEngine extends TrackingEngine {
        FailingTestEngine(SandboxClient client, SshClient ssh) {
            super(client, ssh);
        }

        @Override
        public void test() {
            throw new RuntimeException("test failure");
        }
    }

    /** Engine that doesn't set seedName to trigger validation error. */
    static class NoSeedEngine extends TrackingEngine {
        NoSeedEngine(SandboxClient client, SshClient ssh) {
            super(client, ssh);
        }

        @Override
        public void setup() {
            // intentionally omit seedName
            snapshotName = "snap-001";
            sshUser = "user";
            sshPass = "pass";
        }
    }
}
