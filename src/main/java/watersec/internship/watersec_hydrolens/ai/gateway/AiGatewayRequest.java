package watersec.internship.watersec_hydrolens.ai.gateway;

import java.util.Map;
import java.util.UUID;

/** Stable JSON boundary shared by local adapters and a future FastAPI client. */
public record AiGatewayRequest(
        String schemaVersion,
        UUID correlationId,
        AiOperation operation,
        String requestedAiVersion,
        Map<String, Object> payload) {
}
