package watersec.internship.watersec_hydrolens.simulation.generator.impl;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;
import watersec.internship.watersec_hydrolens.simulation.generator.SeasonModifier;
import watersec.internship.watersec_hydrolens.simulation.config.SimulationProperties;

import java.time.Month;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CalendarSeasonModifier implements SeasonModifier {
    private final SimulationProperties properties;
    public CalendarSeasonModifier(SimulationProperties properties) { this.properties = properties; }
    @Override
    public List<WaterActivityEvent> apply(List<WaterActivityEvent> events) {
        return events.stream().map(event -> {
            Month month = event.timestamp().atZone(ZoneOffset.UTC).getMonth();
            double factor = switch (month) {
                case JUNE, JULY, AUGUST -> properties.getSummerDemandFactor();
                case DECEMBER, JANUARY, FEBRUARY -> properties.getWinterDemandFactor();
                default -> 1.0;
            };
            Map<String, Double> modifiers = new LinkedHashMap<>(event.modifiers());
            modifiers.put("seasonFactor", factor);
            return event.withConsumption(round(event.consumptionLiters() * factor), Map.copyOf(modifiers));
        }).toList();
    }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
