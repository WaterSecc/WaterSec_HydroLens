package watersec.internship.watersec_hydrolens.digitaltwin.dto;

import java.util.List;
import java.util.Map;

/** Independent water-consuming object in a Digital Twin. */
public record TwinComponentDefinition(
        String key,
        String name,
        String type,
        ExpectedDemand expectedDemand,
        ComponentPriority priority,
        List<String> dependencies,
        String waterSource,
        BehaviorModel behaviorModel,
        Map<String, Object> properties) {
}
