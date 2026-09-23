package watersec.internship.watersec_hydrolens.hotel.archetype.generator;

import watersec.internship.watersec_hydrolens.component.entity.ComponentType;

public record SyntheticComponentPlan(
        ComponentType type,
        String name,
        int quantity,
        double baseDailyConsumptionLiters,
        String unit,
        boolean occupancyDependent,
        double efficiencyRating) {
}
