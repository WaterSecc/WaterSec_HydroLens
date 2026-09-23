package watersec.internship.watersec_hydrolens.simulation.normalflow.dto;

import java.util.List;

public record WaterNetworkEdge(
        String id,
        String from,
        String to,
        double currentFlowLitersPerHour,
        double dailyVolumeLiters,
        List<Double> hourlyFlowLitersPerHour) {
}
