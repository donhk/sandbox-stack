package dev.donhk.rest;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetMachineResponse(
        Optional<Machine> machine
) {}
