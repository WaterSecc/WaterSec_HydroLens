package watersec.internship.watersec_hydrolens.simulation.engine;

import watersec.internship.watersec_hydrolens.component.entity.ComponentType;

/**
 * Per-component consumption result produced by the engine.
 */
public record ComponentUsage(
        String componentName,
        ComponentType type,
        double totalConsumptionLiters,
        double percentage
) {
}
