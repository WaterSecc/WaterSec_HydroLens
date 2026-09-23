package watersec.internship.watersec_hydrolens.ai.service;

import watersec.internship.watersec_hydrolens.ai.dto.AiAnalysisRequest;
import watersec.internship.watersec_hydrolens.ai.dto.AiAnalysisResult;

/** Analysis extension point; no AI provider is selected by this contract. */
public interface AiAnalysisPipeline {
    AiAnalysisResult analyze(AiAnalysisRequest request);
}
