package watersec.internship.watersec_hydrolens.simulation.engine;

import watersec.internship.watersec_hydrolens.component.entity.ComponentType;

/**
 * A component as seen by the engine after the hybrid resolution step (stored
 * components with overrides applied, plus any ad-hoc components from the request).
 * Decoupled from JPA entities and web DTOs so the engine stays pure.
 */
public record EffectiveComponent(
        String name,
        ComponentType type,
        int quantity,
        double baseDailyConsumptionLiters,
        boolean occupancyDependent,
        Double efficiencyRating
) {
}
