package watersec.internship.watersec_hydrolens.simulation.normalflow.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.EventSeverity;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.FlowEventType;

public record SimulateFlowEventRequest(
        @NotNull FlowEventType eventType,
        String targetComponent,
        @Positive Double leakFlowLitersPerHour,
        @Min(0) @Max(23) Integer startHour,
        @Min(1) @Max(24) Integer durationHours,
        EventSeverity severity,
        @DecimalMin("0.0") @DecimalMax("1.0") Double highOccupancyRate,
        @Positive Double storageCapacityLiters) {
}
