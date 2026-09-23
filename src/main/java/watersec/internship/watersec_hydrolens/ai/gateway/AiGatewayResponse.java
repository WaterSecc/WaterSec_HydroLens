package watersec.internship.watersec_hydrolens.ai.gateway;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AiGatewayResponse(
        String schemaVersion,
        UUID correlationId,
        AiOperation operation,
        Status status,
        String aiVersion,
        double confidence,
        Map<String, Object> result,
        List<String> warnings) {
    public enum Status { COMPLETED, PARTIAL, NOT_CONFIGURED }
}
