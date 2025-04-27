package dev.donhk.rest.operations;

import dev.donhk.rest.types.OperationState;

public record GetOperationStateResponse(
        OperationState state
) {}
