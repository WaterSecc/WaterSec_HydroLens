package watersec.internship.watersec_hydrolens.ai.service;

import watersec.internship.watersec_hydrolens.ai.gateway.AiGatewayResponse;
import java.util.Map;

public interface AiService {
    AiGatewayResponse generateHotelSpecification(Map<String, Object> context);
    AiGatewayResponse estimateMissingValues(Map<String, Object> context);
    AiGatewayResponse generateRecommendations(Map<String, Object> context);
    AiGatewayResponse generateExplanation(Map<String, Object> context);
    AiGatewayResponse analyzeBenchmark(Map<String, Object> context);
    AiGatewayResponse forecastConsumption(Map<String, Object> context);
    AiGatewayResponse detectAnomalies(Map<String, Object> context);
}
