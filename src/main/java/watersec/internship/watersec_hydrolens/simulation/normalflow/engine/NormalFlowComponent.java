package watersec.internship.watersec_hydrolens.simulation.normalflow.engine;

import watersec.internship.watersec_hydrolens.component.entity.ComponentType;

public record NormalFlowComponent(
        String id,
        String name,
        ComponentType type,
        int quantity,
        double baseDailyConsumptionLiters,
        boolean occupancyDependent,
        double efficiencyRating) {
}
