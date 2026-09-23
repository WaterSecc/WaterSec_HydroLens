package watersec.internship.watersec_hydrolens.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/** A sales-demo description plus optional operator-supplied constraints. */
public record HotelSpecificationRequest(
        @NotBlank @Size(max = 4000) String description,
        Map<String, Object> constraints) {
}
