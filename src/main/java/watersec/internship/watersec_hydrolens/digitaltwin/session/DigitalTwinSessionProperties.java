package watersec.internship.watersec_hydrolens.digitaltwin.session;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;
@Component
@ConfigurationProperties(prefix = "hydrolens.digital-twin-session")
public class DigitalTwinSessionProperties {
    private Duration ttl = Duration.ofMinutes(60);
    private Duration cleanupInterval = Duration.ofMinutes(5);
    private int maxSessions = 100;
    public Duration getTtl() { return ttl; } public void setTtl(Duration value) { ttl = value; }
    public Duration getCleanupInterval() { return cleanupInterval; } public void setCleanupInterval(Duration value) { cleanupInterval = value; }
    public int getMaxSessions() { return maxSessions; } public void setMaxSessions(int value) { maxSessions = value; }
}
