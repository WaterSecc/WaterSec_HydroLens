package watersec.internship.watersec_hydrolens.simulation.generator;

import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;
import java.util.List;

public interface SeasonModifier {
    List<WaterActivityEvent> apply(List<WaterActivityEvent> events);
}
