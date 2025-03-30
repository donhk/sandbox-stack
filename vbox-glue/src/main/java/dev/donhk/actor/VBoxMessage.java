package dev.donhk.actor;

public final class VBoxMessage {
    public record PingRequest(String name, int age) {
    }

    public record PingResponse(String name, int age) {
    }

    public record PingRequest2(String name, int age) {
    }

    public record PingResponse2(String name, int age) {
    }
}
