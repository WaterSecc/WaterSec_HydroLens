package watersec.internship.watersec_hydrolens.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetypeCatalog;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.HotelGenerationHints;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.SeededHotelPlanner;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.SyntheticHotelPlan;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.WaterNetworkEdge;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.WaterNetworkNode;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.NormalFlowComponent;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.NormalFlowInput;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.NormalFlowResult;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.NormalHotelFlowEngine;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.EventSeverity;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.FlowEventType;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.NormalFlowEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalFlowEventTests {
    private final NormalHotelFlowEngine engine = new NormalHotelFlowEngine();
    private List<NormalFlowComponent> components;
    private double occupancy;

    @BeforeEach
    void setUp() {
        SyntheticHotelPlan hotel = new SeededHotelPlanner(new HotelArchetypeCatalog()).plan(
                HotelArchetype.RESORT, new HotelGenerationHints(null, null, null, 42L));
        occupancy = hotel.occupancyRate();
        components = hotel.components().stream().map(component -> new NormalFlowComponent(
                component.type().name(), component.name(), component.type(), component.quantity(),
                component.baseDailyConsumptionLiters(), component.occupancyDependent(),
                component.efficiencyRating())).toList();
    }

    @Test
    void continuousLeakAddsItsFlowForEveryActiveHour() {
        NormalFlowResult baseline = simulate(null);
        NormalFlowEvent leak = new NormalFlowEvent(FlowEventType.CONTINUOUS_LEAK, "LAUNDRY",
                100.0, 0, 24, null, null, null);
        NormalFlowResult event = simulate(leak);

        assertEquals(2400, event.totalConsumptionLiters() - baseline.totalConsumptionLiters(), .1);
    }

    @Test
    void pipeBurstCreatesAHighTemporaryBranchFlow() {
        NormalFlowResult baseline = simulate(null);
        NormalFlowResult burst = simulate(new NormalFlowEvent(FlowEventType.PIPE_BURST, "GUEST_ROOMS",
                null, 12, 2, EventSeverity.HIGH, null, null));

        assertTrue(consumer(burst, ComponentType.GUEST_ROOMS).hourlyConsumptionLiters().get(12)
                > consumer(baseline, ComponentType.GUEST_ROOMS).hourlyConsumptionLiters().get(12) * 3);
        assertEquals(consumer(baseline, ComponentType.GUEST_ROOMS).hourlyConsumptionLiters().get(11),
                consumer(burst, ComponentType.GUEST_ROOMS).hourlyConsumptionLiters().get(11));
    }

    @Test
    void highOccupancyOnlyChangesRelevantOccupancyAssets() {
        NormalFlowResult baseline = simulate(null);
        NormalFlowResult high = simulate(new NormalFlowEvent(FlowEventType.HIGH_OCCUPANCY, null,
                null, 0, 24, null, .98, null));

        assertTrue(consumer(high, ComponentType.GUEST_ROOMS).dailyConsumptionLiters()
                > consumer(baseline, ComponentType.GUEST_ROOMS).dailyConsumptionLiters());
        assertTrue(consumer(high, ComponentType.LAUNDRY).dailyConsumptionLiters()
                > consumer(baseline, ComponentType.LAUNDRY).dailyConsumptionLiters());
        assertEquals(consumer(baseline, ComponentType.POOL).dailyConsumptionLiters(),
                consumer(high, ComponentType.POOL).dailyConsumptionLiters());
        assertEquals(consumer(baseline, ComponentType.IRRIGATION).dailyConsumptionLiters(),
                consumer(high, ComponentType.IRRIGATION).dailyConsumptionLiters());
    }

    @Test
    void heatWavePrimarilyChangesOutdoorAndCoolingAssets() {
        NormalFlowResult baseline = simulate(null);
        NormalFlowResult heat = simulate(new NormalFlowEvent(FlowEventType.HEAT_WAVE, null,
                null, 0, 24, null, null, null));
        Map<ComponentType, Double> changed = heat.nodes().stream()
                .filter(node -> node.type() == WaterNetworkNode.NodeType.CONSUMER)
                .filter(node -> Math.abs(node.dailyConsumptionLiters()
                        - consumer(baseline, node.componentType()).dailyConsumptionLiters()) > .01)
                .collect(Collectors.toMap(WaterNetworkNode::componentType, WaterNetworkNode::dailyConsumptionLiters));

        assertEquals(java.util.Set.of(ComponentType.POOL, ComponentType.IRRIGATION, ComponentType.COOLING_TOWER),
                changed.keySet());
    }

    @Test
    void supplyInterruptionStopsIncomingFlowAndDepletesStorage() {
        NormalFlowResult interrupted = simulate(new NormalFlowEvent(FlowEventType.WATER_SUPPLY_INTERRUPTION,
                null, null, 8, 8, null, null, 100.0));
        WaterNetworkEdge incoming = interrupted.edges().stream()
                .filter(edge -> edge.from().equals("municipal-supply")).findFirst().orElseThrow();

        for (int hour = 8; hour < 16; hour++) assertEquals(0, incoming.hourlyFlowLitersPerHour().get(hour));
        assertEquals(0, interrupted.hourlyStorageLevelLiters().get(15));
        assertTrue(interrupted.remainingAutonomyHours() < 8);
    }

    @Test
    void sameBaselineEventAndSeedAreReproducible() {
        NormalFlowEvent event = new NormalFlowEvent(FlowEventType.HEAT_WAVE, null,
                null, 0, 24, null, null, null);
        assertEquals(simulate(event), simulate(event));
    }

    @Test
    void eventCalculationDoesNotMutateBaselineResult() {
        NormalFlowResult baseline = simulate(null);
        NormalFlowResult snapshot = simulate(null);
        NormalFlowResult event = simulate(new NormalFlowEvent(FlowEventType.CONTINUOUS_LEAK, "LAUNDRY",
                100.0, 0, 24, null, null, null));

        assertEquals(snapshot, baseline);
        assertNotEquals(event, baseline);
    }

    private NormalFlowResult simulate(NormalFlowEvent event) {
        return engine.simulate(new NormalFlowInput(HotelArchetype.RESORT, occupancy, 91, 12, components, event));
    }

    private WaterNetworkNode consumer(NormalFlowResult result, ComponentType type) {
        return result.nodes().stream().filter(node -> node.componentType() == type).findFirst().orElseThrow();
    }
}
