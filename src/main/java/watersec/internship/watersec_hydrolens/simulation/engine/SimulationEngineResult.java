package watersec.internship.watersec_hydrolens.simulation.engine;

import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;

import java.time.Instant;
import java.util.List;

/**
 * Pure output of the simulation engine. Cost is intentionally absent — it depends
 * on the facility water cost and is computed by the service.
 */
public record SimulationEngineResult(
        double totalConsumptionLiters,
        double sustainabilityScore,
        List<ComponentUsage> breakdown,
        List<Double> dailyConsumption,
        List<WaterActivityEvent> events,
        Instant startTime
) {
}
