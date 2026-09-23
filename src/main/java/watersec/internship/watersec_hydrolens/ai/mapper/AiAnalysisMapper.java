package watersec.internship.watersec_hydrolens.ai.mapper;

import watersec.internship.watersec_hydrolens.ai.dto.AiAnalysisResult;

import java.util.Map;

public interface AiAnalysisMapper {
    AiAnalysisResult toResult(Map<String, Object> providerResult);
}
