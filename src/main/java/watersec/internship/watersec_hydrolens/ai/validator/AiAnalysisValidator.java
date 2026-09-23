package watersec.internship.watersec_hydrolens.ai.validator;

import watersec.internship.watersec_hydrolens.ai.dto.AiAnalysisRequest;

public interface AiAnalysisValidator {
    void validate(AiAnalysisRequest request);
}
