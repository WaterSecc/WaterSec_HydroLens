package watersec.internship.watersec_hydrolens.digitaltwin.session;

import watersec.internship.watersec_hydrolens.ai.dto.AiHotelSpecification;
import watersec.internship.watersec_hydrolens.digitaltwin.dto.DigitalTwinDefinition;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.FlowEventComparisonResponse;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.NormalFlowSimulationResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Complete temporary state for one hotel demonstration. */
public record DigitalTwinSession(UUID sessionId, Instant createdAt, Instant lastAccessedAt, Instant expiresAt,
        DigitalTwinSessionStatus status, HotelArchetype archetype, AiHotelSpecification hotelSpecification,
        DigitalTwinDefinition digitalTwin, SessionGenerationMetadata generation,
        NormalFlowSimulationResponse baseline, FlowEventComparisonResponse currentScenario,
        List<FlowEventComparisonResponse> eventHistory) {
    public DigitalTwinSession { eventHistory = eventHistory == null ? List.of() : List.copyOf(eventHistory); }
    public DigitalTwinSession touch(Instant now, Instant expiry) {
        return new DigitalTwinSession(sessionId, createdAt, now, expiry, status, archetype, hotelSpecification,
                digitalTwin, generation, baseline, currentScenario, eventHistory);
    }
    public DigitalTwinSession withBaseline(NormalFlowSimulationResponse value, Instant now, Instant expiry) {
        return new DigitalTwinSession(sessionId, createdAt, now, expiry, status, archetype, hotelSpecification,
                digitalTwin, generation, value, null, List.of());
    }
    public DigitalTwinSession withScenario(FlowEventComparisonResponse value, Instant now, Instant expiry) {
        var history = new java.util.ArrayList<>(eventHistory); history.add(value);
        if (history.size() > 20) history.remove(0);
        return new DigitalTwinSession(sessionId, createdAt, now, expiry, status, archetype, hotelSpecification,
                digitalTwin, generation, baseline, value, history);
    }
    public DigitalTwinSession resetScenario(Instant now, Instant expiry) {
        return new DigitalTwinSession(sessionId, createdAt, now, expiry, status, archetype, hotelSpecification,
                digitalTwin, generation, baseline, null, List.of());
    }
}
