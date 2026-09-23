package watersec.internship.watersec_hydrolens.simulation.normalflow.event;

public record NormalFlowEvent(
        FlowEventType type,
        String targetComponent,
        Double leakFlowLitersPerHour,
        int startHour,
        int durationHours,
        EventSeverity severity,
        Double highOccupancyRate,
        Double storageCapacityLiters) {
}
