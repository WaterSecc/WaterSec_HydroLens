package watersec.internship.watersec_hydrolens.ai.service.impl;

import org.springframework.stereotype.Service;
import watersec.internship.watersec_hydrolens.ai.dto.AiAnalysisRequest;
import watersec.internship.watersec_hydrolens.ai.dto.AiAnalysisResult;
import watersec.internship.watersec_hydrolens.ai.config.AiPlatformProperties;
import watersec.internship.watersec_hydrolens.ai.gateway.AiGateway;
import watersec.internship.watersec_hydrolens.ai.gateway.AiGatewayRequest;
import watersec.internship.watersec_hydrolens.ai.gateway.AiGatewayResponse;
import watersec.internship.watersec_hydrolens.ai.gateway.AiOperation;
import watersec.internship.watersec_hydrolens.ai.service.AiAnalysisPipeline;
import watersec.internship.watersec_hydrolens.ai.service.AiService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiOrchestrator implements AiService, AiAnalysisPipeline {
    private static final String SCHEMA_VERSION = "1.0";
    private final AiGateway gateway;
    private final AiPlatformProperties properties;

    public AiOrchestrator(AiGateway gateway, AiPlatformProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    public AiGatewayResponse generateHotelSpecification(Map<String, Object> c) { return dispatch(AiOperation.GENERATE_HOTEL_SPECIFICATION, c); }
    public AiGatewayResponse estimateMissingValues(Map<String, Object> c) { return dispatch(AiOperation.ESTIMATE_MISSING_VALUES, c); }
    public AiGatewayResponse generateRecommendations(Map<String, Object> c) { return dispatch(AiOperation.GENERATE_RECOMMENDATIONS, c); }
    public AiGatewayResponse generateExplanation(Map<String, Object> c) { return dispatch(AiOperation.GENERATE_EXPLANATION, c); }
    public AiGatewayResponse analyzeBenchmark(Map<String, Object> c) { return dispatch(AiOperation.ANALYZE_BENCHMARK, c); }
    public AiGatewayResponse forecastConsumption(Map<String, Object> c) { return dispatch(AiOperation.FORECAST_CONSUMPTION, c); }
    public AiGatewayResponse detectAnomalies(Map<String, Object> c) { return dispatch(AiOperation.DETECT_ANOMALIES, c); }

    @Override
    public AiAnalysisResult analyze(AiAnalysisRequest request) {
        AiGatewayResponse response = dispatch(AiOperation.DETECT_ANOMALIES, Map.of(
                "facilityId", request.facilityId(), "simulationId", request.simulationId(),
                "telemetryDatasetId", request.telemetryDatasetId(), "context", request.context()));
        return new AiAnalysisResult(request.simulationId(), List.of(), response.result(), response.aiVersion());
    }

    private AiGatewayResponse dispatch(AiOperation operation, Map<String, Object> context) {
        AiGatewayRequest request = new AiGatewayRequest(properties.getContractVersion() == null
                ? SCHEMA_VERSION : properties.getContractVersion(), UUID.randomUUID(), operation,
                properties.getVersion(), context == null ? Map.of() : Map.copyOf(context));
        return gateway.execute(request);
    }
}
