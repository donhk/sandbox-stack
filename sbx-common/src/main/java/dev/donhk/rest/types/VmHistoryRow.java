package dev.donhk.rest.types;

import java.util.List;

public record VmHistoryRow(List<String> labels, List<String> vmCounts) {
}
