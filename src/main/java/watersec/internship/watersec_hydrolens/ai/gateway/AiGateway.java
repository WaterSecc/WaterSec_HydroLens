package watersec.internship.watersec_hydrolens.ai.gateway;

/** Only integration boundary allowed to communicate with a future FastAPI service. */
public interface AiGateway {
    AiGatewayResponse execute(AiGatewayRequest request);
}
