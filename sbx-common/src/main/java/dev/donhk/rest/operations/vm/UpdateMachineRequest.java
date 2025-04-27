package dev.donhk.rest.operations.vm;

import java.time.Instant;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateMachineRequest(
        String uuid,
        Optional<Integer> newTimeSecs,
        Optional<Instant> seenAt
) {}
