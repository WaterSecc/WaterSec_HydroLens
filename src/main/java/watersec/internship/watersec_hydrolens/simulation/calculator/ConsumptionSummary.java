package watersec.internship.watersec_hydrolens.simulation.calculator;

import watersec.internship.watersec_hydrolens.simulation.engine.ComponentUsage;
import java.util.List;

public record ConsumptionSummary(double totalLiters, List<Double> dailyConsumption,
                                 List<ComponentUsage> componentUsage) {
}
