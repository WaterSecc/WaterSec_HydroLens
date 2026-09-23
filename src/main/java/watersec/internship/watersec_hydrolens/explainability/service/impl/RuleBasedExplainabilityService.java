package watersec.internship.watersec_hydrolens.explainability.service.impl;

import org.springframework.stereotype.Service;
import watersec.internship.watersec_hydrolens.explainability.dto.Evidence;
import watersec.internship.watersec_hydrolens.explainability.dto.Explanation;
import watersec.internship.watersec_hydrolens.explainability.service.ExplainabilityService;
import java.util.List;

@Service
public class RuleBasedExplainabilityService implements ExplainabilityService {
    @Override
    public Explanation explain(String reasoning, double confidence, List<Evidence> evidence,
                               List<String> affectedComponents, double savingsLiters, double savingsCost) {
        return new Explanation(reasoning, confidence, List.copyOf(evidence), List.copyOf(affectedComponents),
                new Explanation.ExpectedSavings(savingsLiters, savingsCost), "rules-1.0");
    }
}
