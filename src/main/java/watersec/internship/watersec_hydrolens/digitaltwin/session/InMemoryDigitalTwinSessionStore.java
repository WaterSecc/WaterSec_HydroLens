package watersec.internship.watersec_hydrolens.digitaltwin.session;
import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.common.exception.BadRequestException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;
@Component
public class InMemoryDigitalTwinSessionStore implements DigitalTwinSessionStore {
    private final ConcurrentMap<UUID, DigitalTwinSession> sessions = new ConcurrentHashMap<>();
    private final DigitalTwinSessionProperties properties;
    public InMemoryDigitalTwinSessionStore(DigitalTwinSessionProperties properties) { this.properties = properties; }
    public DigitalTwinSession create(DigitalTwinSession session) {
        deleteExpired(Instant.now());
        if (sessions.size() >= properties.getMaxSessions()) throw new BadRequestException("Temporary session capacity reached");
        if (sessions.putIfAbsent(session.sessionId(), session) != null) throw new IllegalStateException("Duplicate session ID");
        return session;
    }
    public Optional<DigitalTwinSession> find(UUID id) {
        DigitalTwinSession value = sessions.get(id);
        if (value != null && !value.expiresAt().isAfter(Instant.now())) { sessions.remove(id, value); return Optional.empty(); }
        return Optional.ofNullable(value);
    }
    public DigitalTwinSession update(UUID id, UnaryOperator<DigitalTwinSession> operation) {
        DigitalTwinSession value = sessions.compute(id, (key, existing) -> existing == null
                || !existing.expiresAt().isAfter(Instant.now()) ? null : operation.apply(existing));
        if (value == null) throw new DigitalTwinSessionNotFoundException(id);
        return value;
    }
    public boolean delete(UUID id) { return sessions.remove(id) != null; }
    public int deleteExpired(Instant now) { int before = sessions.size(); sessions.entrySet().removeIf(e -> !e.getValue().expiresAt().isAfter(now)); return before - sessions.size(); }
    public int size() { return sessions.size(); }
}
