package watersec.internship.watersec_hydrolens.ai.gateway;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;
import java.util.Map;

/** Explicit no-ML adapter used until a FastAPI HTTP adapter is configured. */
@Component
@ConditionalOnProperty(prefix = "hydrolens.ai", name = "gateway-mode", havingValue = "local-no-ml", matchIfMissing = true)
public class LocalNoMlAiGateway implements AiGateway {
    @Override
    public AiGatewayResponse execute(AiGatewayRequest request) {
        return new AiGatewayResponse(request.schemaVersion(), request.correlationId(), request.operation(),
                AiGatewayResponse.Status.NOT_CONFIGURED, "none", 0.0, Map.of(),
                List.of("AI provider is not configured; use the existing deterministic rule engines"));
    }
}
