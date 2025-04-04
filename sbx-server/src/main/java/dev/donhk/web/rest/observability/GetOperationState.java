package dev.donhk.web.rest.observability;

import dev.donhk.rest.GetOperationStateRequest;
import dev.donhk.rest.GetOperationStateResponse;
import dev.donhk.rest.OperationState;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class GetOperationState implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        GetOperationStateRequest request = ctx.bodyAsClass(GetOperationStateRequest.class);

        // Example: Retrieve operation state logic
        OperationState state = switch (request.operationId()) {
            case "123" -> OperationState.STARTED;
            case "456" -> OperationState.FAILED;
            default -> OperationState.TIMEOUT;
        };

        ctx.json(new GetOperationStateResponse(state));
    }
}
