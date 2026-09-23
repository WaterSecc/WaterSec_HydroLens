package watersec.internship.watersec_hydrolens.simulation.event;

import watersec.internship.watersec_hydrolens.component.entity.ComponentType;

import java.time.Instant;
import java.util.Map;

public record HumanActivity(
        Instant timestamp,
        ActivityType type,
        String component,
        ComponentType componentType,
        int occurrences,
        Map<String, Object> attributes) {
}
