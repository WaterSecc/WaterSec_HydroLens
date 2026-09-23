package watersec.internship.watersec_hydrolens.digitaltwin.session;

import org.junit.jupiter.api.Test;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDigitalTwinSessionStoreTests {
    @Test
    void storesUpdatesAndDeletesTemporarySessions() {
        var properties = new DigitalTwinSessionProperties();
        var store = new InMemoryDigitalTwinSessionStore(properties);
        UUID id = UUID.randomUUID(); Instant now = Instant.now();
        var session = new DigitalTwinSession(id, now, now, now.plusSeconds(60),
                DigitalTwinSessionStatus.READY, HotelArchetype.RESORT, null, null, null, null, null, List.of());
        store.create(session);
        assertThat(store.find(id)).isPresent();
        store.update(id, value -> value.touch(now.plusSeconds(1), now.plusSeconds(61)));
        assertThat(store.find(id).orElseThrow().lastAccessedAt()).isEqualTo(now.plusSeconds(1));
        assertThat(store.delete(id)).isTrue();
        assertThat(store.find(id)).isEmpty();
    }

    @Test
    void removesExpiredSessions() {
        var store = new InMemoryDigitalTwinSessionStore(new DigitalTwinSessionProperties());
        UUID id = UUID.randomUUID(); Instant now = Instant.now();
        store.create(new DigitalTwinSession(id, now, now, now.plusMillis(10), DigitalTwinSessionStatus.READY,
                HotelArchetype.CITY_HOTEL, null, null, null, null, null, List.of()));
        assertThat(store.deleteExpired(now.plusSeconds(1))).isEqualTo(1);
        assertThat(store.size()).isZero();
    }
}
