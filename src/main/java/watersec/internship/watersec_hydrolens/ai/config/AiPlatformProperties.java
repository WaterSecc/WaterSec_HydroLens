package watersec.internship.watersec_hydrolens.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hydrolens.ai")
public class AiPlatformProperties {
    private String version = "none";
    private String gatewayMode = "local-no-ml";
    private String contractVersion = "1.0";
    private String baseUrl = "https://router.huggingface.co/v1";
    private String token;
    private String model = "meta-llama/Llama-3.1-8B-Instruct:nscale";
    private int connectTimeoutSeconds = 5;
    private int readTimeoutSeconds = 30;
    public String getVersion() { return version; }
    public void setVersion(String value) { version = value; }
    public String getGatewayMode() { return gatewayMode; }
    public void setGatewayMode(String value) { gatewayMode = value; }
    public String getContractVersion() { return contractVersion; }
    public void setContractVersion(String value) { contractVersion = value; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String value) { baseUrl = value; }
    public String getToken() { return token; }
    public void setToken(String value) { token = value; }
    public String getModel() { return model; }
    public void setModel(String value) { model = value; }
    public int getConnectTimeoutSeconds() { return connectTimeoutSeconds; }
    public void setConnectTimeoutSeconds(int value) { connectTimeoutSeconds = value; }
    public int getReadTimeoutSeconds() { return readTimeoutSeconds; }
    public void setReadTimeoutSeconds(int value) { readTimeoutSeconds = value; }
}
