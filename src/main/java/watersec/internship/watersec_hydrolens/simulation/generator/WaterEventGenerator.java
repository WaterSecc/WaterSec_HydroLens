package watersec.internship.watersec_hydrolens.simulation.generator;

import watersec.internship.watersec_hydrolens.simulation.engine.EffectiveComponent;
import watersec.internship.watersec_hydrolens.simulation.event.HumanActivity;
import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;

import java.util.List;

public interface WaterEventGenerator {
    List<WaterActivityEvent> generate(List<HumanActivity> activities, List<EffectiveComponent> components);
}
