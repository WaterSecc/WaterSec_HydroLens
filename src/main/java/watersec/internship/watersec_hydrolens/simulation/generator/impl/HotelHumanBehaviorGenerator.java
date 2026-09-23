package watersec.internship.watersec_hydrolens.simulation.generator.impl;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.simulation.config.SimulationProperties;
import watersec.internship.watersec_hydrolens.simulation.engine.EffectiveComponent;
import watersec.internship.watersec_hydrolens.simulation.engine.SimulationEngineInput;
import watersec.internship.watersec_hydrolens.simulation.event.ActivityType;
import watersec.internship.watersec_hydrolens.simulation.event.HumanActivity;
import watersec.internship.watersec_hydrolens.simulation.event.OccupancySnapshot;
import watersec.internship.watersec_hydrolens.simulation.generator.HumanBehaviorGenerator;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HotelHumanBehaviorGenerator implements HumanBehaviorGenerator {
    private final SimulationProperties properties;
    public HotelHumanBehaviorGenerator(SimulationProperties properties) { this.properties = properties; }

    @Override
    public List<HumanActivity> generate(SimulationEngineInput input, List<OccupancySnapshot> occupancy) {
        List<HumanActivity> activities = new ArrayList<>();
        EffectiveComponent rooms = first(input, ComponentType.GUEST_ROOMS);
        for (OccupancySnapshot day : occupancy) {
            if (rooms != null && day.guests() > 0) {
                add(activities, day, 15, ActivityType.CHECK_IN, rooms, day.guests());
                add(activities, day, 7, ActivityType.SHOWER, rooms,
                        count(day.guests() * properties.getShowersPerGuestDay() * 0.65));
                add(activities, day, 20, ActivityType.SHOWER, rooms,
                        count(day.guests() * properties.getShowersPerGuestDay() * 0.35));
                int flushes = count(day.guests() * properties.getFlushesPerGuestDay());
                for (int hour : List.of(7, 12, 18, 22)) add(activities, day, hour,
                        ActivityType.TOILET_FLUSH, rooms, Math.max(1, flushes / 4));
                int washes = count(day.guests() * properties.getHandWashesPerGuestDay());
                for (int hour : List.of(8, 13, 19, 23)) add(activities, day, hour,
                        ActivityType.HAND_WASH, rooms, Math.max(1, washes / 4));
            }
            for (EffectiveComponent component : input.components()) {
                switch (component.type()) {
                    case LAUNDRY -> add(activities, day, 10, ActivityType.LAUNDRY_CYCLE, component,
                            Math.max(1, count(day.occupiedUnits() * 0.7)));
                    case RESTAURANT, KITCHEN -> {
                        add(activities, day, 8, ActivityType.RESTAURANT_SERVICE, component, Math.max(1, day.guests()));
                        add(activities, day, 13, ActivityType.RESTAURANT_SERVICE, component, Math.max(1, day.guests()));
                        add(activities, day, 20, ActivityType.RESTAURANT_SERVICE, component, Math.max(1, day.guests()));
                    }
                    case POOL -> add(activities, day, 15, ActivityType.POOL_USAGE, component,
                            Math.max(1, count(day.guests() * 0.30)));
                    case IRRIGATION -> add(activities, day, 5, ActivityType.IRRIGATION, component, component.quantity());
                    case SPA -> add(activities, day, 16, ActivityType.SPA_USAGE, component,
                            Math.max(1, count(day.guests() * 0.08)));
                    case COOLING_TOWER -> add(activities, day, 12, ActivityType.HVAC_COOLING, component, component.quantity());
                    case OTHER -> add(activities, day, 11, ActivityType.OTHER, component, component.quantity());
                    default -> { }
                }
            }
        }
        return activities.stream().sorted(java.util.Comparator.comparing(HumanActivity::timestamp)).toList();
    }

    private void add(List<HumanActivity> target, OccupancySnapshot day, int hour, ActivityType type,
                     EffectiveComponent component, int occurrences) {
        if (occurrences > 0) target.add(new HumanActivity(day.timestamp().plus(hour, ChronoUnit.HOURS), type,
                component.name(), component.type(), occurrences, Map.of("occupancyRate", day.occupancyRate())));
    }
    private EffectiveComponent first(SimulationEngineInput input, ComponentType type) {
        return input.components().stream().filter(c -> c.type() == type).findFirst().orElse(null);
    }
    private int count(double value) { return Math.max(0, (int) Math.round(value)); }
}
