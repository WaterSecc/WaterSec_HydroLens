package watersec.internship.watersec_hydrolens.simulation.generator.impl;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.simulation.config.SimulationProperties;
import watersec.internship.watersec_hydrolens.simulation.engine.EffectiveComponent;
import watersec.internship.watersec_hydrolens.simulation.engine.SimulationEngineInput;
import watersec.internship.watersec_hydrolens.simulation.event.ActivityType;
import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;
import watersec.internship.watersec_hydrolens.simulation.generator.LeakInjector;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class DeterministicLeakInjector implements LeakInjector {
    private final SimulationProperties properties;
    public DeterministicLeakInjector(SimulationProperties properties) { this.properties = properties; }

    @Override
    public List<WaterActivityEvent> inject(List<WaterActivityEvent> events, SimulationEngineInput input, Instant startTime) {
        List<WaterActivityEvent> result = new ArrayList<>(events);
        if (input.components().isEmpty()) return result;
        Random random = new Random(input.seed() ^ 0x5DEECE66DL);
        for (int day = 0; day < input.durationDays(); day++) {
            if (random.nextDouble() >= properties.getLeakProbabilityPerDay()) continue;
            EffectiveComponent target = input.components().get(random.nextInt(input.components().size()));
            int startHour = 1 + random.nextInt(18);
            int hours = 2 + random.nextInt(7);
            for (int hour = 0; hour < hours; hour++) {
                double liters = properties.getLeakLitersPerHour();
                result.add(new WaterActivityEvent(startTime.plus(day, ChronoUnit.DAYS)
                        .plus(startHour + hour, ChronoUnit.HOURS), ActivityType.LEAK,
                        target.name().toLowerCase().replace(' ', '-'), target.name(), target.type(), 1,
                        liters, 3600, liters / 60.0, Map.of("leak", 1.0)));
            }
        }
        return result.stream().sorted(java.util.Comparator.comparing(WaterActivityEvent::timestamp)).toList();
    }
}
