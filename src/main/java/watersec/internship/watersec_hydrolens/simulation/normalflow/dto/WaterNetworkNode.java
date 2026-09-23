package watersec.internship.watersec_hydrolens.simulation.normalflow.dto;

import watersec.internship.watersec_hydrolens.component.entity.ComponentType;

import java.util.List;

public record WaterNetworkNode(
        String id,
        NodeType type,
        String label,
        ComponentType componentType,
        double currentFlowLitersPerHour,
        double dailyConsumptionLiters,
        double shareOfTotalPercentage,
        List<Double> hourlyConsumptionLiters) {

    public enum NodeType { SOURCE, STORAGE, CONSUMER }
}
