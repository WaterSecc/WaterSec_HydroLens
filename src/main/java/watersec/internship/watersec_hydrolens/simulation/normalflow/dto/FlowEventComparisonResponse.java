package watersec.internship.watersec_hydrolens.simulation.normalflow.dto;

import watersec.internship.watersec_hydrolens.simulation.normalflow.event.FlowEventType;

import java.util.List;
import java.util.UUID;

public record FlowEventComparisonResponse(
        UUID baselineSimulationId,
        UUID eventSimulationId,
        FlowEventType eventType,
        double baselineConsumptionLiters,
        double eventConsumptionLiters,
        double absoluteDifferenceLiters,
        double percentageDifference,
        double equivalentMonthlyDifferenceLiters,
        double lostWaterLiters,
        double lostWaterCost,
        String currency,
        List<String> affectedAssets,
        NormalFlowSimulationResponse baseline,
        NormalFlowSimulationResponse event) {
}
