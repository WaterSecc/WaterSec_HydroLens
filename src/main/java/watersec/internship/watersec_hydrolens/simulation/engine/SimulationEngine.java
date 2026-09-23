package watersec.internship.watersec_hydrolens.simulation.engine;

/**
 * Strategy for turning a facility's resolved components into synthetic water
 * consumption. Implementations are pure (no persistence), so different facility
 * types or future AI-driven engines can be swapped in without touching callers.
 */
public interface SimulationEngine {

    SimulationEngineResult generate(SimulationEngineInput input);
}
