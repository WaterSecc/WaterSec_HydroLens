package watersec.internship.watersec_hydrolens.explainability.dto;

import java.util.List;

public record Explanation(
        String reasoning,
        double confidence,
        List<Evidence> evidence,
        List<String> affectedComponents,
        ExpectedSavings expectedSavings,
        String explanationVersion) {
    public record ExpectedSavings(double litersPerYear, double costPerYear) {}
}
