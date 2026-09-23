package watersec.internship.watersec_hydrolens.simulation.generator;

import watersec.internship.watersec_hydrolens.simulation.engine.SimulationEngineInput;
import watersec.internship.watersec_hydrolens.simulation.event.HumanActivity;
import watersec.internship.watersec_hydrolens.simulation.event.OccupancySnapshot;

import java.util.List;

public interface HumanBehaviorGenerator {
    List<HumanActivity> generate(SimulationEngineInput input, List<OccupancySnapshot> occupancy);
}
