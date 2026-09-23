package watersec.internship.watersec_hydrolens.ai.dto;

import java.util.Map;

public record AnalysisFinding(
        String type,
        String summary,
        double confidence,
        Map<String, Object> evidence) {
}
