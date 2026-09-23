package watersec.internship.watersec_hydrolens.digitaltwin.session.service;

import org.springframework.stereotype.Service;
import watersec.internship.watersec_hydrolens.common.exception.BadRequestException;
import watersec.internship.watersec_hydrolens.digitaltwin.session.*;
import watersec.internship.watersec_hydrolens.digitaltwin.session.dto.CreateDigitalTwinSessionRequest;
import watersec.internship.watersec_hydrolens.simulation.calculator.CostCalculator;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.*;
import watersec.internship.watersec_hydrolens.simulation.normalflow.engine.*;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.*;

import java.time.Instant;
import java.util.*;

@Service
public class DigitalTwinSessionService {
    private final DigitalTwinSessionStore store;
    private final DigitalTwinSessionProperties properties;
    private final SessionHotelGenerator generator;
    private final SessionDigitalTwinFactory twinFactory;
    private final NormalHotelFlowEngine engine;
    private final CostCalculator costCalculator;
    public DigitalTwinSessionService(DigitalTwinSessionStore store, DigitalTwinSessionProperties properties,
            SessionHotelGenerator generator, SessionDigitalTwinFactory twinFactory,
            NormalHotelFlowEngine engine, CostCalculator costCalculator) {
        this.store = store; this.properties = properties; this.generator = generator;
        this.twinFactory = twinFactory; this.engine = engine; this.costCalculator = costCalculator;
    }
    public DigitalTwinSession create(CreateDigitalTwinSessionRequest request) {
        UUID id = UUID.randomUUID(); Instant now = Instant.now();
        SessionHotelGeneration generated = generator.generate(request);
        var twin = twinFactory.build(id, generated);
        var metadata = new SessionGenerationMetadata(generated.mode(), generated.model(),
                request.archetype().name(), "1.0", generated.seed(), now);
        return store.create(new DigitalTwinSession(id, now, now, expiry(now), DigitalTwinSessionStatus.READY,
                request.archetype(), generated.specification(), twin, metadata, null, null, List.of()));
    }
    public DigitalTwinSession get(UUID id) {
        store.find(id).orElseThrow(() -> new DigitalTwinSessionNotFoundException(id));
        Instant now = Instant.now(); return store.update(id, value -> value.touch(now, expiry(now)));
    }
    public void delete(UUID id) { if (!store.delete(id)) throw new DigitalTwinSessionNotFoundException(id); }
    public DigitalTwinSession resetScenario(UUID id) {
        Instant now = Instant.now(); return store.update(id, value -> value.resetScenario(now, expiry(now)));
    }
    public DigitalTwinSession runBaseline(UUID id, NormalFlowSimulationRequest request) {
        int hour = request != null && request.currentHour() != null ? request.currentHour() : 12;
        DigitalTwinSession current = get(id);
        long seed = request != null && request.seed() != null ? request.seed() : current.generation().seed();
        NormalFlowResult result = engine.simulate(new NormalFlowInput(current.archetype(),
                current.hotelSpecification().occupancyRate(), seed, hour, components(current)));
        NormalFlowSimulationResponse response = response(UUID.randomUUID(), id, seed, hour, result,
                costCalculator.calculate(result.totalConsumptionLiters(), current.hotelSpecification().waterCostPerLiter()));
        Instant now = Instant.now(); return store.update(id, value -> value.withBaseline(response, now, expiry(now)));
    }
    public DigitalTwinSession simulateEvent(UUID id, SimulateFlowEventRequest request) {
        DigitalTwinSession current = get(id);
        if (current.baseline() == null) throw new BadRequestException("Run the baseline simulation before generating an event");
        NormalFlowEvent event = event(request, current);
        var base = current.baseline();
        NormalFlowResult result = engine.simulate(new NormalFlowInput(current.archetype(),
                current.hotelSpecification().occupancyRate(), base.seed(), base.currentHour(), components(current), event));
        double eventCost = costCalculator.calculate(result.totalConsumptionLiters(), current.hotelSpecification().waterCostPerLiter());
        var eventResponse = response(UUID.randomUUID(), id, base.seed(), base.currentHour(), result, eventCost);
        double difference = round(result.totalConsumptionLiters() - base.totalConsumptionLiters());
        double percentage = base.totalConsumptionLiters() == 0 ? 0 : round(difference / base.totalConsumptionLiters() * 100);
        double lost = event.type() == FlowEventType.CONTINUOUS_LEAK || event.type() == FlowEventType.PIPE_BURST
                ? Math.max(0, difference) : 0;
        var comparison = new FlowEventComparisonResponse(base.simulationId(), eventResponse.simulationId(), event.type(),
                base.totalConsumptionLiters(), result.totalConsumptionLiters(), difference, percentage,
                round(difference * 30), round(lost), costCalculator.calculate(lost,
                current.hotelSpecification().waterCostPerLiter()), "TND", affected(base, eventResponse), base, eventResponse);
        Instant now = Instant.now(); return store.update(id, value -> value.withScenario(comparison, now, expiry(now)));
    }
    private List<NormalFlowComponent> components(DigitalTwinSession session) {
        List<NormalFlowComponent> values = new ArrayList<>();
        for (int i = 0; i < session.hotelSpecification().components().size(); i++) {
            var c = session.hotelSpecification().components().get(i);
            String key = c.type().name().toLowerCase().replace('_', '-') + "-" + (i + 1);
            values.add(new NormalFlowComponent(key, c.name(), c.type(), c.quantity(), c.baseDailyConsumptionLiters(),
                    c.occupancyDependent(), c.efficiencyRating()));
        }
        return List.copyOf(values);
    }
    private NormalFlowEvent event(SimulateFlowEventRequest r, DigitalTwinSession session) {
        if (r == null || r.eventType() == null) throw new BadRequestException("eventType is required");
        boolean targeted = r.eventType() == FlowEventType.CONTINUOUS_LEAK || r.eventType() == FlowEventType.PIPE_BURST;
        if (targeted && (r.targetComponent() == null || r.targetComponent().isBlank())) throw new BadRequestException("targetComponent is required");
        if (targeted && components(session).stream().noneMatch(c -> c.id().equalsIgnoreCase(r.targetComponent())
                || c.name().equalsIgnoreCase(r.targetComponent()) || c.type().name().equalsIgnoreCase(r.targetComponent())))
            throw new BadRequestException("Target component does not belong to this session");
        int start = r.startHour() == null ? (r.eventType() == FlowEventType.PIPE_BURST ? 12 : 0) : r.startHour();
        int duration = r.durationHours() == null ? (r.eventType() == FlowEventType.PIPE_BURST ? 2 : 24) : r.durationHours();
        if (start + duration > 24) throw new BadRequestException("Event must finish within the same 24-hour simulation day");
        return new NormalFlowEvent(r.eventType(), r.targetComponent(), r.leakFlowLitersPerHour(), start, duration,
                r.severity(), r.highOccupancyRate(), r.storageCapacityLiters());
    }
    private NormalFlowSimulationResponse response(UUID simulationId, UUID sessionId, long seed, int hour,
            NormalFlowResult result, double cost) {
        return new NormalFlowSimulationResponse(simulationId, sessionId, seed, hour, result.timestamp(),
                result.totalConsumptionLiters(), cost, result.hourlyTotalConsumptionLiters(), result.nodes(),
                result.edges(), result.hourlyStorageLevelLiters(), result.remainingAutonomyHours());
    }
    private List<String> affected(NormalFlowSimulationResponse baseline, NormalFlowSimulationResponse event) {
        List<String> values = new ArrayList<>();
        for (WaterNetworkNode node : event.nodes()) baseline.nodes().stream().filter(b -> b.id().equals(node.id())).findFirst()
                .filter(b -> Math.abs(node.dailyConsumptionLiters() - b.dailyConsumptionLiters()) > .01)
                .ifPresent(ignored -> values.add(node.label()));
        return List.copyOf(values);
    }
    private Instant expiry(Instant now) { return now.plus(properties.getTtl()); }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
