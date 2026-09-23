package watersec.internship.watersec_hydrolens.digitaltwin.session.service;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.ai.dto.AiHotelSpecification;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.digitaltwin.dto.*;
import java.util.*;

/** Converts an immutable hotel specification directly into a persistence-free twin. */
@Component
public class SessionDigitalTwinFactory {
    private static final String SUPPLY = "main-supply";
    public DigitalTwinDefinition build(UUID sessionId, SessionHotelGeneration generation) {
        AiHotelSpecification hotel = generation.specification();
        List<TwinComponentDefinition> components = new ArrayList<>();
        for (int index = 0; index < hotel.components().size(); index++) {
            var item = hotel.components().get(index);
            String key = item.type().name().toLowerCase().replace('_', '-') + "-" + (index + 1);
            double baseline = item.quantity() * item.baseDailyConsumptionLiters()
                    * (item.occupancyDependent() ? hotel.occupancyRate() : 1.0);
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("quantity", item.quantity()); details.put("unit", item.unit());
            details.put("baseDailyConsumptionLiters", item.baseDailyConsumptionLiters());
            details.put("efficiencyRating", item.efficiencyRating());
            components.add(new TwinComponentDefinition(key, item.name(), item.type().name(),
                    new ExpectedDemand(round(baseline), round(baseline / 24 * 2.5), round(baseline * .65),
                            round(baseline * 1.45), .80), priority(item.type()), List.of(SUPPLY), "municipal",
                    new BehaviorModel("DETERMINISTIC_PROFILE", item.occupancyDependent(), peakHours(item.type()),
                            Map.of("weekday", 1.0, "weekend", 1.08, "hotWeather", 1.15), "1.0"), Map.copyOf(details)));
        }
        List<ComponentRelationship> relationships = components.stream().map(c -> new ComponentRelationship(
                SUPPLY, c.key(), ComponentRelationship.RelationshipType.SUPPLIES, priorityWeight(c.priority()))).toList();
        List<WaterNetworkDefinition.WaterNetworkNode> nodes = new ArrayList<>();
        nodes.add(new WaterNetworkDefinition.WaterNetworkNode("source", "SOURCE", "Municipal supply"));
        nodes.add(new WaterNetworkDefinition.WaterNetworkNode(SUPPLY, "MAIN_METER", "Main water supply"));
        components.forEach(c -> nodes.add(new WaterNetworkDefinition.WaterNetworkNode(c.key(), "COMPONENT", c.name())));
        List<WaterNetworkDefinition.WaterNetworkLink> links = new ArrayList<>();
        double peak = components.stream().mapToDouble(c -> c.expectedDemand().peakLitersPerHour()).sum();
        links.add(new WaterNetworkDefinition.WaterNetworkLink("source", SUPPLY, round(peak * 1.25)));
        components.forEach(c -> links.add(new WaterNetworkDefinition.WaterNetworkLink(SUPPLY, c.key(),
                round(c.expectedDemand().peakLitersPerHour() * 1.25))));
        TwinFacility facility = new TwinFacility(sessionId, hotel.hotelName(), "HOTEL", hotel.country(), hotel.city(),
                hotel.stars(), hotel.floors(), null, "municipal");
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("hotelArchetype", hotel.archetype().name()); attributes.put("generationSeed", generation.seed());
        attributes.put("normalOccupancyRate", hotel.occupancyRate()); attributes.put("generationMode", generation.mode());
        attributes.put("aiModel", generation.model()); attributes.put("temporarySession", true);
        return new DigitalTwinDefinition(sessionId, "HOTEL", facility, List.copyOf(components), relationships,
                new WaterNetworkDefinition("municipal", List.copyOf(nodes), List.copyOf(links)),
                Map.copyOf(attributes), "hotel-session-twin-1.0");
    }
    private ComponentPriority priority(ComponentType type) { return switch (type) {
        case GUEST_ROOMS -> ComponentPriority.CRITICAL; case LAUNDRY, RESTAURANT, KITCHEN -> ComponentPriority.HIGH;
        case COOLING_TOWER, SPA -> ComponentPriority.MEDIUM; case POOL, IRRIGATION, OTHER -> ComponentPriority.LOW; }; }
    private List<Integer> peakHours(ComponentType type) { return switch (type) {
        case GUEST_ROOMS -> List.of(6,7,8,19,20,21); case LAUNDRY -> List.of(8,9,10,14,15);
        case RESTAURANT, KITCHEN -> List.of(7,8,12,13,19,20); case IRRIGATION -> List.of(5,6);
        default -> List.of(10,11,12,13,14,15,16,17); }; }
    private double priorityWeight(ComponentPriority value) { return switch (value) { case CRITICAL -> 1; case HIGH -> .8; case MEDIUM -> .6; case LOW -> .4; }; }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
