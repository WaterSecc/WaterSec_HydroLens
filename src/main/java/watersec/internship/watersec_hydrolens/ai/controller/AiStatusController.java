package watersec.internship.watersec_hydrolens.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import watersec.internship.watersec_hydrolens.ai.config.AiPlatformProperties;
import watersec.internship.watersec_hydrolens.ai.dto.AiStatusResponse;
import watersec.internship.watersec_hydrolens.common.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/ai/status")
public class AiStatusController {
    private final AiPlatformProperties properties;

    public AiStatusController(AiPlatformProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AiStatusResponse>> status() {
        boolean llamaMode = "huggingface-llama".equals(properties.getGatewayMode());
        boolean hasToken = properties.getToken() != null && !properties.getToken().isBlank()
                && !properties.getToken().contains("replace_with");
        boolean configured = llamaMode && hasToken;
        String message = configured
                ? "Llama adapter is configured; a successful generation is required to verify inference"
                : "AI is not configured; hotel generation uses deterministic archetypes";
        return ResponseEntity.ok(ApiResponse.success(new AiStatusResponse(configured,
                properties.getGatewayMode(), properties.getModel(), properties.getVersion(),
                "Generates hotel configuration only; deterministic engines calculate water and leaks", message)));
    }
}
