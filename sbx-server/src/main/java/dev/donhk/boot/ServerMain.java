package dev.donhk.boot;

import dev.donhk.server.SandboxerApp;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        final SandboxerApp sandboxerApp = SandboxerApp.newInstance(args);
        sandboxerApp.start();
    }
}
