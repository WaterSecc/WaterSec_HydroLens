package watersec.internship.watersec_hydrolens.digitaltwin.session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
@Component
public class DigitalTwinSessionCleanupJob {
    private final DigitalTwinSessionStore store;
    public DigitalTwinSessionCleanupJob(DigitalTwinSessionStore store) { this.store = store; }
    @Scheduled(fixedDelayString = "${hydrolens.digital-twin-session.cleanup-interval:PT5M}")
    public void removeExpiredSessions() { store.deleteExpired(Instant.now()); }
}
