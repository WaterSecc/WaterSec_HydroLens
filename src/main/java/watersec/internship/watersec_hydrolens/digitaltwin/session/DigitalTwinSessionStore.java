package watersec.internship.watersec_hydrolens.digitaltwin.session;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
public interface DigitalTwinSessionStore {
    DigitalTwinSession create(DigitalTwinSession session);
    Optional<DigitalTwinSession> find(UUID sessionId);
    DigitalTwinSession update(UUID sessionId, UnaryOperator<DigitalTwinSession> operation);
    boolean delete(UUID sessionId);
    int deleteExpired(Instant now);
    int size();
}
