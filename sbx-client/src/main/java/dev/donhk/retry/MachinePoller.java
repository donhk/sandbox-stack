package dev.donhk.retry;

import dev.donhk.client.SandboxClient;
import dev.donhk.exception.SandboxException;
import dev.donhk.rest.types.Machine;
import dev.donhk.rest.types.MachineState;
import org.tinylog.Logger;

import java.time.Duration;

public class MachinePoller {

    private static final long POLL_INTERVAL_MS = 5_000;

    private final SandboxClient client;
    private final String uuid;

    public MachinePoller(SandboxClient client, String uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    /**
     * Blocks until the machine reaches {@code target} state, or throws on timeout / FAILED state.
     */
    public Machine waitForState(MachineState target, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        MachineState lastLogged = null;
        while (System.currentTimeMillis() < deadline) {
            Machine m = client.getMachine(uuid);
            if (m.machineState() != lastLogged) {
                Logger.info("Machine {} state={}", uuid, m.machineState());
                lastLogged = m.machineState();
            }
            if (m.machineState() == MachineState.FAILED || m.machineState() == MachineState.TERMINATED) {
                throw new SandboxException("Machine " + uuid + " entered terminal state: " + m.machineState());
            }
            if (m.machineState() == target) {
                return m;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        throw new SandboxException("Timed out after " + timeout + " waiting for machine " + uuid + " to reach " + target);
    }

    /**
     * Blocks until the machine has a non-empty vmIpAddress, or throws on timeout.
     */
    public String waitForIp(Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        Logger.info("Waiting for IP on machine {}", uuid);
        while (System.currentTimeMillis() < deadline) {
            Machine m = client.getMachine(uuid);
            if (m.machineState() == MachineState.FAILED || m.machineState() == MachineState.TERMINATED) {
                throw new SandboxException("Machine " + uuid + " entered terminal state: " + m.machineState());
            }
            if (m.vmIpAddress().isPresent() && !m.vmIpAddress().get().isBlank()) {
                return m.vmIpAddress().get();
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        throw new SandboxException("Timed out after " + timeout + " waiting for IP on machine " + uuid);
    }
}
