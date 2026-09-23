package watersec.internship.watersec_hydrolens.simulation.calculator.impl;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.simulation.calculator.ConsumptionCalculator;
import watersec.internship.watersec_hydrolens.simulation.calculator.ConsumptionSummary;
import watersec.internship.watersec_hydrolens.simulation.engine.ComponentUsage;
import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventConsumptionCalculator implements ConsumptionCalculator {
    @Override
    public ConsumptionSummary calculate(List<WaterActivityEvent> events, Instant startTime, int durationDays) {
        double[] daily = new double[durationDays];
        Map<ComponentKey, Double> totals = new LinkedHashMap<>();
        for (WaterActivityEvent event : events) {
            int day = (int) ChronoUnit.DAYS.between(startTime, event.timestamp());
            if (day >= 0 && day < durationDays) daily[day] += event.consumptionLiters();
            totals.merge(new ComponentKey(event.component(), event.componentType()), event.consumptionLiters(), Double::sum);
        }
        double total = events.stream().mapToDouble(WaterActivityEvent::consumptionLiters).sum();
        List<ComponentUsage> usage = totals.entrySet().stream().map(entry -> new ComponentUsage(
                entry.getKey().name, entry.getKey().type, round(entry.getValue()),
                total == 0 ? 0 : round(entry.getValue() / total * 100.0))).toList();
        List<Double> days = new ArrayList<>(durationDays);
        for (double value : daily) days.add(round(value));
        return new ConsumptionSummary(round(total), List.copyOf(days), usage);
    }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
    private record ComponentKey(String name, ComponentType type) {}
}
