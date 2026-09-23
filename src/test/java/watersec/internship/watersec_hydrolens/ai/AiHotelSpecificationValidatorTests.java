package watersec.internship.watersec_hydrolens.ai;

import org.junit.jupiter.api.Test;
import watersec.internship.watersec_hydrolens.ai.dto.AiHotelSpecification;
import watersec.internship.watersec_hydrolens.ai.exception.AiAnalysisException;
import watersec.internship.watersec_hydrolens.ai.validator.AiHotelSpecificationValidator;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiHotelSpecificationValidatorTests {
    private final AiHotelSpecificationValidator validator = new AiHotelSpecificationValidator();

    @Test
    void rejectsOutOfRangeComponentConsumptionWithActionableDetails() {
        var invalid = specification(0.72, 0.8);
        var component = invalid.components().get(0);
        var badComponent = new AiHotelSpecification.AiHotelComponentSpecification(
                component.name(), component.type(), component.quantity(), 0.001,
                component.unit(), component.occupancyDependent(), component.efficiencyRating());
        var badHotel = new AiHotelSpecification(invalid.hotelName(), invalid.country(), invalid.city(),
                invalid.archetype(), invalid.stars(), invalid.floors(), invalid.rooms(), invalid.occupancyRate(),
                invalid.waterCostPerLiter(), List.of(badComponent), invalid.assumptions());

        assertThatThrownBy(() -> validator.validate(badHotel))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("Guest Rooms")
                .hasMessageContaining("0.001")
                .hasMessageContaining("per-unit daily value");
    }

    @Test
    void acceptsAConfigurationThatCanFeedTheDeterministicEngine() {
        assertDoesNotThrow(() -> validator.validate(specification(.72, .82)));
    }

    @Test
    void rejectsOutOfRangeModelOutputBeforeSimulation() {
        assertThrows(AiAnalysisException.class, () -> validator.validate(specification(72, .82)));
        assertThrows(AiAnalysisException.class, () -> validator.validate(specification(.72, 8.2)));
    }

    @Test
    void rejectsHotelScalePlaceholderDemandAndZeroTariff() {
        var valid = specification(.72, .82);
        var laundry = new AiHotelSpecification.AiHotelComponentSpecification(
                "Laundry", ComponentType.LAUNDRY, 4, 5, "machines", true, .7);
        var badLaundry = new AiHotelSpecification(valid.hotelName(), valid.country(), valid.city(),
                valid.archetype(), valid.stars(), valid.floors(), valid.rooms(), valid.occupancyRate(),
                valid.waterCostPerLiter(), List.of(valid.components().get(0), laundry), valid.assumptions());
        assertThatThrownBy(() -> validator.validate(badLaundry))
                .hasMessageContaining("LAUNDRY")
                .hasMessageContaining("between 500.0 and 10000.0");

        var zeroTariff = new AiHotelSpecification(valid.hotelName(), valid.country(), valid.city(),
                valid.archetype(), valid.stars(), valid.floors(), valid.rooms(), valid.occupancyRate(),
                0, valid.components(), valid.assumptions());
        assertThatThrownBy(() -> validator.validate(zeroTariff))
                .hasMessageContaining("waterCostPerLiter");
    }

    @Test
    void rejectsRoomQuantityThatDoesNotMatchHotel() {
        var valid = specification(.72, .82);
        var rooms = valid.components().get(0);
        var mismatch = new AiHotelSpecification.AiHotelComponentSpecification(
                rooms.name(), rooms.type(), 100, rooms.baseDailyConsumptionLiters(),
                rooms.unit(), rooms.occupancyDependent(), rooms.efficiencyRating());
        var hotel = new AiHotelSpecification(valid.hotelName(), valid.country(), valid.city(),
                valid.archetype(), valid.stars(), valid.floors(), valid.rooms(), valid.occupancyRate(),
                valid.waterCostPerLiter(), List.of(mismatch), valid.assumptions());
        assertThatThrownBy(() -> validator.validate(hotel))
                .hasMessageContaining("quantity must equal");
    }

    private AiHotelSpecification specification(double occupancy, double efficiency) {
        return new AiHotelSpecification("WaterSec Demo Hotel", "Tunisia", "Tunis",
                HotelArchetype.CITY_HOTEL, 4, 8, 120, occupancy, .003,
                List.of(new AiHotelSpecification.AiHotelComponentSpecification(
                        "Guest Rooms", ComponentType.GUEST_ROOMS, 120, 300,
                        "room", true, efficiency)), List.of("Typical city hotel profile"));
    }
}
