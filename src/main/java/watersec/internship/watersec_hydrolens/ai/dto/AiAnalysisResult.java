package watersec.internship.watersec_hydrolens.ai.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Provider-neutral analysis result consumed by recommendation adapters. */
public record AiAnalysisResult(
        UUID simulationId,
        List<AnalysisFinding> findings,
        Map<String, Object> forecasts,
        String modelVersion) {
}
