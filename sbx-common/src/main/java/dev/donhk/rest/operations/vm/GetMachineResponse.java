package dev.donhk.rest.operations.vm;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.donhk.rest.types.Machine;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetMachineResponse(
        Optional<Machine> machine
) {}
