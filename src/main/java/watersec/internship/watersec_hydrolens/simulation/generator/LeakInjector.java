package watersec.internship.watersec_hydrolens.simulation.generator;

import watersec.internship.watersec_hydrolens.simulation.engine.SimulationEngineInput;
import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;

import java.time.Instant;
import java.util.List;

public interface LeakInjector {
    List<WaterActivityEvent> inject(List<WaterActivityEvent> events, SimulationEngineInput input, Instant startTime);
}
