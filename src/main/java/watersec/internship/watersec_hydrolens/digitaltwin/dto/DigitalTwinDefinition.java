package watersec.internship.watersec_hydrolens.digitaltwin.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Immutable, persistence-neutral description passed to scenario intelligence. */
public record DigitalTwinDefinition(
        UUID facilityId,
        String facilityType,
        TwinFacility facility,
        List<TwinComponentDefinition> components,
        List<ComponentRelationship> relationships,
        WaterNetworkDefinition waterNetwork,
        Map<String, Object> attributes,
        String modelVersion) {
}
