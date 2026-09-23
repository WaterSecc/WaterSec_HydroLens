package watersec.internship.watersec_hydrolens.simulation.generator.impl;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.simulation.config.SimulationProperties;
import watersec.internship.watersec_hydrolens.simulation.engine.SimulationEngineInput;
import watersec.internship.watersec_hydrolens.simulation.event.OccupancySnapshot;
import watersec.internship.watersec_hydrolens.simulation.generator.OccupancyGenerator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class HotelOccupancyGenerator implements OccupancyGenerator {
    private final SimulationProperties properties;
    public HotelOccupancyGenerator(SimulationProperties properties) { this.properties = properties; }

    @Override
    public List<OccupancySnapshot> generate(SimulationEngineInput input, Instant startTime) {
        int capacity = input.components().stream().filter(c -> c.type() == ComponentType.GUEST_ROOMS)
                .mapToInt(c -> c.quantity()).sum();
        Random random = new Random(input.seed());
        List<OccupancySnapshot> snapshots = new ArrayList<>(input.durationDays());
        for (int day = 0; day < input.durationDays(); day++) {
            double variation = 0.94 + random.nextDouble() * 0.12;
            double rate = Math.max(0, Math.min(1, input.occupancyRate() * variation));
            int occupied = (int) Math.round(capacity * rate);
            int guests = (int) Math.round(occupied * properties.getGuestsPerOccupiedRoom());
            snapshots.add(new OccupancySnapshot(startTime.plus(day, ChronoUnit.DAYS), occupied, guests, rate));
        }
        return List.copyOf(snapshots);
    }
}
