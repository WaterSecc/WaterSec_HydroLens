package watersec.internship.watersec_hydrolens.digitaltwin.session.service;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import watersec.internship.watersec_hydrolens.ai.config.AiPlatformProperties;
import watersec.internship.watersec_hydrolens.ai.dto.AiHotelSpecification;
import watersec.internship.watersec_hydrolens.ai.exception.AiAnalysisException;
import watersec.internship.watersec_hydrolens.ai.service.AiService;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.digitaltwin.session.dto.CreateDigitalTwinSessionRequest;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.SeededHotelPlanner;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.SyntheticHotelPlan;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.HotelGenerationHints;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Produces a validated hotel snapshot without writing to any database. */
@Component
public class SessionHotelGenerator {
    private final SeededHotelPlanner planner;
    private final AiService aiService;
    private final AiPlatformProperties properties;
    private final ObjectMapper objectMapper;
    public SessionHotelGenerator(SeededHotelPlanner planner, AiService aiService,
                                 AiPlatformProperties properties, ObjectMapper objectMapper) {
        this.planner = planner; this.aiService = aiService; this.properties = properties; this.objectMapper = objectMapper;
    }
    public SessionHotelGeneration generate(CreateDigitalTwinSessionRequest request) {
        HotelGenerationHints input = new HotelGenerationHints(request.name(), request.country(), request.city(), request.seed());
        SyntheticHotelPlan fallback = planner.plan(request.archetype(), input);
        if (!"huggingface-llama".equals(properties.getGatewayMode())) {
            return new SessionHotelGeneration(fromPlan(fallback), fallback.seed(), "DETERMINISTIC", "none");
        }
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("requestedArchetype", request.archetype().name());
        context.put("country", value(request.country(), fallback.country()));
        context.put("city", value(request.city(), fallback.city()));
        context.put("hotelName", value(request.name(), fallback.name()));
        context.put("seed", fallback.seed());
        context.put("instruction", "Create one plausible sales-demo hotel and honor the selected archetype profile.");
        AiHotelSpecification specification = objectMapper.convertValue(
                aiService.generateHotelSpecification(context).result(), AiHotelSpecification.class);
        if (specification.archetype() != request.archetype()) {
            throw new AiAnalysisException("Llama returned " + specification.archetype()
                    + " for requested " + request.archetype());
        }
        return new SessionHotelGeneration(specification, fallback.seed(), "LLAMA", properties.getModel());
    }
    private AiHotelSpecification fromPlan(SyntheticHotelPlan plan) {
        var components = plan.components().stream().map(c -> new AiHotelSpecification.AiHotelComponentSpecification(
                c.name(), c.type(), c.quantity(), c.baseDailyConsumptionLiters(), c.unit(),
                c.occupancyDependent(), c.efficiencyRating())).toList();
        int rooms = plan.components().stream().filter(c -> c.type() == ComponentType.GUEST_ROOMS)
                .mapToInt(c -> c.quantity()).findFirst().orElse(1);
        return new AiHotelSpecification(plan.name(), plan.country(), plan.city(), plan.archetype(),
                plan.stars(), plan.floors(), rooms, plan.occupancyRate(), plan.waterCostPerLiter(),
                components, List.of("Generated from the deterministic archetype profile because remote AI is disabled."));
    }
    private String value(String supplied, String fallback) { return supplied == null || supplied.isBlank() ? fallback : supplied; }
}
