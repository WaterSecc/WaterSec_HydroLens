package watersec.internship.watersec_hydrolens.simulation.generator;

import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;
import java.util.List;

public interface WeatherModifier {
    List<WaterActivityEvent> apply(List<WaterActivityEvent> events);
}
