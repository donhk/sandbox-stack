package dev.donhk.rest.operations.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.donhk.rest.types.Network;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PinMachineRequest(
        String uuid,
        String name,
        Network network,
        boolean locked
) {
}
