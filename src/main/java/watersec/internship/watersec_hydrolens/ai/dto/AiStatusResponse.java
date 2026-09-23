package watersec.internship.watersec_hydrolens.ai.dto;

public record AiStatusResponse(
        boolean configured,
        String mode,
        String model,
        String version,
        String role,
        String message) {
}
