package watersec.internship.watersec_hydrolens.simulation.normalflow.engine;

import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.WaterNetworkEdge;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.WaterNetworkNode;

import java.time.Instant;
import java.util.List;

public record NormalFlowResult(
        Instant timestamp,
        double totalConsumptionLiters,
        List<Double> hourlyTotalConsumptionLiters,
        List<WaterNetworkNode> nodes,
        List<WaterNetworkEdge> edges,
        List<Double> hourlyStorageLevelLiters,
        Double remainingAutonomyHours) {
}
