package watersec.internship.watersec_hydrolens.simulation.calculator;

import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;
import java.time.Instant;
import java.util.List;

public interface ConsumptionCalculator {
    ConsumptionSummary calculate(List<WaterActivityEvent> events, Instant startTime, int durationDays);
}
