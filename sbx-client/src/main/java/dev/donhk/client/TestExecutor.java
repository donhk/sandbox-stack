package dev.donhk.client;

import org.tinylog.Logger;

/**
 * Orchestrates the full {@link TestEngine} lifecycle in the correct order,
 * guaranteeing that {@code cleanup()} is always called even when earlier
 * phases throw.
 *
 * <p>Usage:
 * <pre>{@code
 * SandboxClient client = new SandboxClient("http://localhost:8008");
 * TestExecutor executor = new TestExecutor();
 * executor.execute(new MyTest(client));
 * }</pre>
 */
public class TestExecutor {

    /**
     * Executes the full test lifecycle:
     * <ol>
     *   <li>{@code setup()} — user configures seed, credentials, etc.</li>
     *   <li>{@code setupEnvironment()} — framework clones + starts VM, waits for SSH</li>
     *   <li>{@code testEnvSetup()} — optional user hook after VM is ready</li>
     *   <li>{@code test()} — user runs test logic via SSH</li>
     *   <li>{@code resultVerification()} — user asserts outcomes</li>
     *   <li>{@code cleanup()} — framework destroys VM (always runs)</li>
     * </ol>
     */
    public void execute(TestEngine engine) throws Exception {
        engine.setup();
        try {
            engine.setupEnvironment();
            engine.testEnvSetup();
            engine.test();
            engine.resultVerification();
        } finally {
            safeCleanup(engine);
        }
    }

    private void safeCleanup(TestEngine engine) {
        try {
            engine.cleanup();
        } catch (Exception e) {
            Logger.error("Unexpected error during cleanup", e);
        }
    }
}
