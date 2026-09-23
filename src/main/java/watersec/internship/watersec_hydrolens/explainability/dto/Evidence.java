package watersec.internship.watersec_hydrolens.explainability.dto;

public record Evidence(String metric, Object observedValue, Object referenceValue, String source) {
}
