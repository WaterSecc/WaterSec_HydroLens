package watersec.internship.watersec_hydrolens.ai.prompt;

import org.junit.jupiter.api.Test;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HotelArchetypePromptCatalogTests {
    private final HotelArchetypePromptCatalog catalog = new HotelArchetypePromptCatalog();

    @Test
    void definesAPromptSlotForEverySupportedArchetype() {
        for (HotelArchetype archetype : HotelArchetype.values()) {
            assertThat(catalog.promptFor(archetype))
                    .contains("REFERENCE EXAMPLE")
                    .contains("GUEST_ROOMS")
                    .contains("L/room/day");
        }
    }

    @Test
    void rejectsMissingArchetype() {
        assertThatThrownBy(() -> catalog.promptFor(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("archetype is required");
    }
}
