package watersec.internship.watersec_hydrolens.explainability.service;

import watersec.internship.watersec_hydrolens.explainability.dto.Evidence;
import watersec.internship.watersec_hydrolens.explainability.dto.Explanation;
import java.util.List;

public interface ExplainabilityService {
    Explanation explain(String reasoning, double confidence, List<Evidence> evidence,
                        List<String> affectedComponents, double savingsLiters, double savingsCost);
}
