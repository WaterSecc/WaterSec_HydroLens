package watersec.internship.watersec_hydrolens.simulation.generator.impl;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.simulation.event.ActivityType;
import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;
import watersec.internship.watersec_hydrolens.simulation.generator.WeatherModifier;
import watersec.internship.watersec_hydrolens.simulation.config.SimulationProperties;

import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeterministicWeatherModifier implements WeatherModifier {
    private final SimulationProperties properties;
    public DeterministicWeatherModifier(SimulationProperties properties) { this.properties = properties; }
    @Override
    public List<WaterActivityEvent> apply(List<WaterActivityEvent> events) {
        return events.stream().map(event -> {
            int day = event.timestamp().atZone(ZoneOffset.UTC).getDayOfYear();
            double temperature = properties.getAverageTemperatureC() + properties.getTemperatureAmplitudeC()
                    * Math.sin((day - 172) * 2.0 * Math.PI / 365.0);
            double factor = switch (event.activityType()) {
                case POOL_USAGE, IRRIGATION, HVAC_COOLING -> temperature >= properties.getHotWeatherThresholdC()
                        ? properties.getHotWeatherDemandFactor() : temperature <= properties.getColdWeatherThresholdC()
                        ? properties.getColdWeatherDemandFactor() : 1.0;
                default -> 1.0;
            };
            return modify(event, factor, "weather", temperature);
        }).toList();
    }

    private WaterActivityEvent modify(WaterActivityEvent event, double factor, String key, double value) {
        Map<String, Double> modifiers = new LinkedHashMap<>(event.modifiers());
        modifiers.put(key, value); modifiers.put("weatherFactor", factor);
        return event.withConsumption(round(event.consumptionLiters() * factor), Map.copyOf(modifiers));
    }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
