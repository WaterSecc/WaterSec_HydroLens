package watersec.internship.watersec_hydrolens.ai.dto;

import java.util.Map;
import java.util.UUID;

/** References simulation and telemetry outputs without coupling to persistence documents. */
public record AiAnalysisRequest(
        UUID facilityId,
        UUID simulationId,
        String telemetryDatasetId,
        Map<String, Object> context) {
}
