package dev.donhk.pojos;

public record VMPortRow(
        String uuid,
        String name,
        int hostPort,
        int vmPort
) {
}
