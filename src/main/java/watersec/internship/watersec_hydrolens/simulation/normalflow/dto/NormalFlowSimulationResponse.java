package watersec.internship.watersec_hydrolens.simulation.normalflow.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NormalFlowSimulationResponse(
        UUID simulationId,
        UUID facilityId,
        long seed,
        int currentHour,
        Instant timestamp,
        double totalConsumptionLiters,
        double estimatedCost,
        List<Double> hourlyTotalConsumptionLiters,
        List<WaterNetworkNode> nodes,
        List<WaterNetworkEdge> edges,
        List<Double> hourlyStorageLevelLiters,
        Double remainingAutonomyHours) {
}
