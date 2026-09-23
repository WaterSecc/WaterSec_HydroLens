package watersec.internship.watersec_hydrolens.digitaltwin.dto;

import java.util.List;
import java.util.Map;

public record BehaviorModel(
        String modelType,
        boolean occupancyDependent,
        List<Integer> peakHours,
        Map<String, Double> factors,
        String version) {
}
