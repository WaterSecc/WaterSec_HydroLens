package watersec.internship.watersec_hydrolens.simulation.engine;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.simulation.calculator.ConsumptionCalculator;
import watersec.internship.watersec_hydrolens.simulation.calculator.ConsumptionSummary;
import watersec.internship.watersec_hydrolens.simulation.event.HumanActivity;
import watersec.internship.watersec_hydrolens.simulation.event.OccupancySnapshot;
import watersec.internship.watersec_hydrolens.simulation.event.WaterActivityEvent;
import watersec.internship.watersec_hydrolens.simulation.generator.HumanBehaviorGenerator;
import watersec.internship.watersec_hydrolens.simulation.generator.LeakInjector;
import watersec.internship.watersec_hydrolens.simulation.generator.OccupancyGenerator;
import watersec.internship.watersec_hydrolens.simulation.generator.SeasonModifier;
import watersec.internship.watersec_hydrolens.simulation.generator.WaterEventGenerator;
import watersec.internship.watersec_hydrolens.simulation.generator.WeatherModifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Event-driven hotel simulation. No total is calculated before activities exist. */
@Component
public class HotelSimulationEngine implements SimulationEngine {
    private static final Instant REPRODUCIBLE_EPOCH = Instant.parse("2026-01-01T00:00:00Z");
    private final OccupancyGenerator occupancyGenerator;
    private final HumanBehaviorGenerator humanBehaviorGenerator;
    private final WaterEventGenerator waterEventGenerator;
    private final LeakInjector leakInjector;
    private final WeatherModifier weatherModifier;
    private final SeasonModifier seasonModifier;
    private final ConsumptionCalculator consumptionCalculator;

    public HotelSimulationEngine(OccupancyGenerator occupancyGenerator,
                                 HumanBehaviorGenerator humanBehaviorGenerator,
                                 WaterEventGenerator waterEventGenerator,
                                 LeakInjector leakInjector,
                                 WeatherModifier weatherModifier,
                                 SeasonModifier seasonModifier,
                                 ConsumptionCalculator consumptionCalculator) {
        this.occupancyGenerator = occupancyGenerator;
        this.humanBehaviorGenerator = humanBehaviorGenerator;
        this.waterEventGenerator = waterEventGenerator;
        this.leakInjector = leakInjector;
        this.weatherModifier = weatherModifier;
        this.seasonModifier = seasonModifier;
        this.consumptionCalculator = consumptionCalculator;
    }

    @Override
    public SimulationEngineResult generate(SimulationEngineInput input) {
        Instant startTime = REPRODUCIBLE_EPOCH.plus(Math.floorMod(input.seed(), 365), ChronoUnit.DAYS);
        List<OccupancySnapshot> occupancy = occupancyGenerator.generate(input, startTime);
        List<HumanActivity> activities = humanBehaviorGenerator.generate(input, occupancy);
        List<WaterActivityEvent> waterEvents = waterEventGenerator.generate(activities, input.components());
        waterEvents = seasonModifier.apply(waterEvents);
        waterEvents = weatherModifier.apply(waterEvents);
        waterEvents = leakInjector.inject(waterEvents, input, startTime);
        ConsumptionSummary consumption = consumptionCalculator.calculate(waterEvents, startTime, input.durationDays());
        return new SimulationEngineResult(consumption.totalLiters(), sustainabilityScore(input.components()),
                consumption.componentUsage(), consumption.dailyConsumption(), waterEvents, startTime);
    }

    private double sustainabilityScore(List<EffectiveComponent> components) {
        double weight = 0;
        double weightedEfficiency = 0;
        for (EffectiveComponent component : components) {
            double componentWeight = component.quantity() * component.baseDailyConsumptionLiters();
            weightedEfficiency += (component.efficiencyRating() == null ? 0 : component.efficiencyRating()) * componentWeight;
            weight += componentWeight;
        }
        return weight == 0 ? 0 : Math.round(weightedEfficiency / weight * 10000.0) / 100.0;
    }
}
