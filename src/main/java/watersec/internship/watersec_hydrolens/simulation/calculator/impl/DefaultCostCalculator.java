package watersec.internship.watersec_hydrolens.simulation.calculator.impl;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.simulation.calculator.CostCalculator;

@Component
public class DefaultCostCalculator implements CostCalculator {
    @Override
    public double calculate(double consumptionLiters, double unitCost) {
        return Math.round(consumptionLiters * unitCost * 100.0) / 100.0;
    }
}
