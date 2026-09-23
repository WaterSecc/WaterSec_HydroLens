package watersec.internship.watersec_hydrolens.simulation.engine;

import java.util.List;

/**
 * Pure input to the simulation engine: the resolved components and run parameters.
 * The {@code seed} makes generation deterministic/reproducible for a given input.
 */
public record SimulationEngineInput(
        List<EffectiveComponent> components,
        int durationDays,
        double occupancyRate,
        long seed
) {
}
