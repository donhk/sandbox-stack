package dev.donhk.ssh;

import dev.donhk.exception.SshException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.Duration;

public class SshClient implements AutoCloseable {

    private final String host;
    private final int port;
    private final String user;
    private final String pass;

    private SSHClient underlying;

    public SshClient(String host, int port, String user, String pass) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
    }

    /**
     * Establishes the SSH connection. Idempotent if already connected.
     */
    public void connect() throws IOException {
        if (underlying != null && underlying.isConnected()) {
            return;
        }
        underlying = new SSHClient();
        underlying.addHostKeyVerifier(new PromiscuousVerifier());
        underlying.connect(host, port);
        underlying.authPassword(user, pass);
        Logger.info("SSH connected to {}:{}", host, port);
    }

    /**
     * Polls until SSH is reachable and a command succeeds, up to {@code timeout}.
     * Leaves the client connected on success.
     */
    public void waitForReady(Duration timeout) throws InterruptedException, SshException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                connect();
                execute("hostname");
                Logger.info("SSH ready on {}:{}", host, port);
                return;
            } catch (Exception e) {
                Logger.debug("SSH not ready on {}:{} — {}", host, port, e.getMessage());
                disconnectQuietly();
            }
            Thread.sleep(10_000);
        }
        throw new SshException("SSH on " + host + ":" + port + " not ready within " + timeout);
    }

    /**
     * Executes a single command and returns its stdout as a trimmed string.
     */
    public String execute(String command) throws IOException {
        ensureConnected();
        try (Session session = underlying.startSession()) {
            Session.Command cmd = session.exec(command);
            String output = new String(cmd.getInputStream().readAllBytes()).trim();
            cmd.join();
            Logger.debug("exec `{}` -> exit={}", command, cmd.getExitStatus());
            return output;
        }
    }

    /**
     * Copies a local file to the remote VM via SCP.
     */
    public void copyTo(String localPath, String remotePath) throws IOException {
        ensureConnected();
        underlying.newSCPFileTransfer().upload(new FileSystemFile(localPath), remotePath);
        Logger.debug("SCP {} -> {}:{}", localPath, host, remotePath);
    }

    /**
     * Downloads a remote file from the VM to a local path via SCP.
     */
    public void copyFrom(String remotePath, String localPath) throws IOException {
        ensureConnected();
        underlying.newSCPFileTransfer().download(remotePath, new FileSystemFile(localPath));
        Logger.debug("SCP {}:{} -> {}", host, remotePath, localPath);
    }

    @Override
    public void close() {
        disconnectQuietly();
    }

    private void ensureConnected() throws IOException {
        if (underlying == null || !underlying.isConnected()) {
            connect();
        }
    }

    private void disconnectQuietly() {
        if (underlying != null) {
            try {
                underlying.disconnect();
            } catch (Exception ignored) {
            }
            underlying = null;
        }
    }
}
