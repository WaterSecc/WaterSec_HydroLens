package watersec.internship.watersec_hydrolens.simulation.event;

import watersec.internship.watersec_hydrolens.component.entity.ComponentType;

import java.time.Instant;
import java.util.Map;

/** Atomic source of consumption. Totals are derived only by summing these events. */
public record WaterActivityEvent(
        Instant timestamp,
        ActivityType activityType,
        String location,
        String component,
        ComponentType componentType,
        int occurrences,
        double consumptionLiters,
        int durationSeconds,
        double flowRateLitersPerMinute,
        Map<String, Double> modifiers) {

    public WaterActivityEvent withConsumption(double liters, Map<String, Double> newModifiers) {
        return new WaterActivityEvent(timestamp, activityType, location, component, componentType,
                occurrences, liters, durationSeconds, flowRateLitersPerMinute, newModifiers);
    }
}
