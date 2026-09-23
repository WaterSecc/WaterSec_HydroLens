package watersec.internship.watersec_hydrolens.simulation.generator.impl;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.simulation.config.SimulationProperties;
import watersec.internship.watersec_hydrolens.simulation.engine.EffectiveComponent;
import watersec.internship.watersec_hydrolens.simulation.event.ActivityType;
import watersec.internship.watersec_hydrolens.simulation.event.HumanActivity;
import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;
import watersec.internship.watersec_hydrolens.simulation.generator.WaterEventGenerator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ActivityWaterEventGenerator implements WaterEventGenerator {
    private final SimulationProperties properties;
    public ActivityWaterEventGenerator(SimulationProperties properties) { this.properties = properties; }

    @Override
    public List<WaterActivityEvent> generate(List<HumanActivity> activities, List<EffectiveComponent> components) {
        Map<String, EffectiveComponent> byName = components.stream()
                .collect(Collectors.toMap(EffectiveComponent::name, Function.identity(), (a, b) -> a));
        return activities.stream().map(activity -> toWaterEvent(activity, byName.get(activity.component()))).toList();
    }

    private WaterActivityEvent toWaterEvent(HumanActivity activity, EffectiveComponent component) {
        double perOccurrence = litersPerOccurrence(activity.type(), component);
        double efficiency = component == null || component.efficiencyRating() == null ? 0 : component.efficiencyRating();
        double efficiencyFactor = 1.0 - Math.min(1.0, Math.max(0.0, efficiency)) * 0.50;
        double liters = perOccurrence * activity.occurrences() * efficiencyFactor;
        int duration = durationSeconds(activity.type());
        double flow = duration == 0 ? 0 : liters / duration * 60.0;
        return new WaterActivityEvent(activity.timestamp(), activity.type(), location(activity), activity.component(),
                activity.componentType(), activity.occurrences(), round(liters), duration, round(flow),
                Map.of("efficiency", efficiencyFactor));
    }

    private double litersPerOccurrence(ActivityType type, EffectiveComponent component) {
        return switch (type) {
            case CHECK_IN -> 0.0;
            case SHOWER -> properties.getShowerLiters();
            case TOILET_FLUSH -> properties.getToiletFlushLiters();
            case HAND_WASH -> properties.getHandWashLiters();
            case LAUNDRY_CYCLE -> properties.getLaundryCycleLiters();
            case RESTAURANT_SERVICE -> properties.getRestaurantMealLiters();
            case POOL_USAGE -> properties.getPoolGuestLiters();
            case IRRIGATION -> properties.getIrrigationLitersPerUnit();
            case SPA_USAGE -> properties.getSpaVisitLiters();
            case HVAC_COOLING, OTHER -> component == null ? 0.0 : component.baseDailyConsumptionLiters();
            case LEAK -> properties.getLeakLitersPerHour();
        };
    }

    private int durationSeconds(ActivityType type) {
        return switch (type) {
            case SHOWER -> 480; case TOILET_FLUSH -> 30; case HAND_WASH -> 20;
            case LAUNDRY_CYCLE -> 2700; case RESTAURANT_SERVICE -> 3600; case POOL_USAGE -> 3600;
            case IRRIGATION -> 1800; case SPA_USAGE -> 3600; case HVAC_COOLING, OTHER, LEAK -> 3600;
            case CHECK_IN -> 0;
        };
    }
    private String location(HumanActivity activity) { return activity.component().toLowerCase().replace(' ', '-'); }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
