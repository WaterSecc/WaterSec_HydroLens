package watersec.internship.watersec_hydrolens.simulation.generator;

import watersec.internship.watersec_hydrolens.simulation.engine.SimulationEngineInput;
import watersec.internship.watersec_hydrolens.simulation.event.OccupancySnapshot;

import java.time.Instant;
import java.util.List;

public interface OccupancyGenerator {
    List<OccupancySnapshot> generate(SimulationEngineInput input, Instant startTime);
}
