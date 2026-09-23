package watersec.internship.watersec_hydrolens.simulation;

import org.junit.jupiter.api.Test;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetypeCatalog;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.HotelGenerationHints;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.SeededHotelPlanner;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.SyntheticHotelPlan;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.WaterNetworkNode;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.NormalFlowComponent;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.NormalFlowInput;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.NormalFlowResult;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.NormalHotelFlowEngine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalHotelFlowEngineTests {
    private final SeededHotelPlanner planner = new SeededHotelPlanner(new HotelArchetypeCatalog());
    private final NormalHotelFlowEngine engine = new NormalHotelFlowEngine();

    @Test
    void networkIsBuiltFromEveryGeneratedHotelComponent() {
        SyntheticHotelPlan hotel = hotel(HotelArchetype.RESORT, 42);
        NormalFlowResult result = simulate(hotel, 91);
        long consumers = result.nodes().stream()
                .filter(node -> node.type() == WaterNetworkNode.NodeType.CONSUMER).count();

        assertEquals(hotel.components().size(), consumers);
        assertEquals(hotel.components().size() + 1, result.edges().size());
        hotel.components().forEach(component -> assertTrue(result.nodes().stream()
                .anyMatch(node -> node.componentType() == component.type())));
    }

    @Test
    void allNodeAndEdgeFlowsAreNonNegative() {
        NormalFlowResult result = simulate(hotel(HotelArchetype.LUXURY_RESORT, 42), 91);

        assertTrue(result.nodes().stream().allMatch(node -> node.currentFlowLitersPerHour() >= 0
                && node.dailyConsumptionLiters() >= 0
                && node.hourlyConsumptionLiters().stream().allMatch(value -> value >= 0)));
        assertTrue(result.edges().stream().allMatch(edge -> edge.currentFlowLitersPerHour() >= 0
                && edge.dailyVolumeLiters() >= 0
                && edge.hourlyFlowLitersPerHour().stream().allMatch(value -> value >= 0)));
    }

    @Test
    void componentAndHourlyTotalsSumToDailyConsumption() {
        NormalFlowResult result = simulate(hotel(HotelArchetype.RESORT, 42), 91);
        double consumerTotal = result.nodes().stream()
                .filter(node -> node.type() == WaterNetworkNode.NodeType.CONSUMER)
                .mapToDouble(WaterNetworkNode::dailyConsumptionLiters).sum();
        double hourlyTotal = result.hourlyTotalConsumptionLiters().stream().mapToDouble(Double::doubleValue).sum();
        double branchTotal = result.edges().stream().filter(edge -> edge.from().equals("main-storage"))
                .mapToDouble(edge -> edge.dailyVolumeLiters()).sum();

        assertEquals(result.totalConsumptionLiters(), consumerTotal, .02);
        assertEquals(result.totalConsumptionLiters(), hourlyTotal, .05);
        assertEquals(result.totalConsumptionLiters(), branchTotal, .02);
    }

    @Test
    void dailyFlowIsReproducibleForTheSameHotelAndSeed() {
        SyntheticHotelPlan hotel = hotel(HotelArchetype.RESORT, 42);
        assertEquals(simulate(hotel, 91), simulate(hotel, 91));
    }

    @Test
    void resortAndCityHotelHaveDifferentFlowDistributions() {
        NormalFlowResult city = simulate(hotel(HotelArchetype.CITY_HOTEL, 42), 91);
        NormalFlowResult resort = simulate(hotel(HotelArchetype.RESORT, 42), 91);

        assertTrue(leisureShare(resort) > leisureShare(city));
        assertTrue(resort.nodes().size() > city.nodes().size());
    }

    private SyntheticHotelPlan hotel(HotelArchetype archetype, long seed) {
        return planner.plan(archetype, new HotelGenerationHints(null, null, null, seed));
    }

    private NormalFlowResult simulate(SyntheticHotelPlan hotel, long seed) {
        List<NormalFlowComponent> components = hotel.components().stream().map(component ->
                new NormalFlowComponent(component.type().name(), component.name(), component.type(),
                        component.quantity(), component.baseDailyConsumptionLiters(),
                        component.occupancyDependent(), component.efficiencyRating())).toList();
        return engine.simulate(new NormalFlowInput(hotel.archetype(), hotel.occupancyRate(), seed, 12, components));
    }

    private double leisureShare(NormalFlowResult result) {
        return result.nodes().stream().filter(node -> node.type() == WaterNetworkNode.NodeType.CONSUMER)
                .filter(node -> node.componentType() == ComponentType.POOL || node.componentType() == ComponentType.SPA
                        || node.componentType() == ComponentType.IRRIGATION)
                .mapToDouble(WaterNetworkNode::shareOfTotalPercentage).sum();
    }
}
