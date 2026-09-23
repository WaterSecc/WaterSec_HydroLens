package watersec.internship.watersec_hydrolens.simulation.normalflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record NormalFlowSimulationRequest(
        @Min(1) Long seed,
        @Min(0) @Max(23) Integer currentHour) {
}
