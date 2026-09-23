package watersec.internship.watersec_hydrolens.ai.validator;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.ai.dto.AiHotelSpecification;
import watersec.internship.watersec_hydrolens.ai.exception.AiAnalysisException;

import java.util.HashSet;
import java.util.Set;

@Component
public class AiHotelSpecificationValidator {
    public void validate(AiHotelSpecification value) {
        if (value == null) fail("Hugging Face returned an empty hotel specification");
        if (blank(value.hotelName()) || blank(value.country()) || blank(value.city())) fail("Hotel identity and location are required");
        if (value.archetype() == null) fail("A supported hotel archetype is required");
        range(value.stars(), 1, 5, "stars");
        range(value.floors(), 1, 150, "floors");
        range(value.rooms(), 1, 10_000, "rooms");
        range(value.occupancyRate(), 0.05, 1.0, "occupancyRate");
        range(value.waterCostPerLiter(), 0.0001, 1.0, "waterCostPerLiter");
        if (value.components() == null || value.components().isEmpty() || value.components().size() > 50) fail("Between 1 and 50 components are required");
        Set<String> names = new HashSet<>();
        int guestRoomComponents = 0;
        for (AiHotelSpecification.AiHotelComponentSpecification component : value.components()) {
            if (component == null || blank(component.name()) || component.type() == null) fail("Every component needs a name and supported type");
            if (!names.add(component.name().strip().toLowerCase())) fail("Component names must be unique");
            range(component.quantity(), 1, 20_000, "component quantity");
            if (component.type() == watersec.internship.watersec_hydrolens.component.entity.ComponentType.GUEST_ROOMS) {
                guestRoomComponents++;
                if (component.quantity() != value.rooms()) fail("GUEST_ROOMS quantity must equal the declared hotel room count");
            }
            componentConsumptionRange(component, value.rooms());
            range(component.efficiencyRating(), 0.0, 1.0, "efficiencyRating");
            if (blank(component.unit()) || component.unit().length() > 40) fail("Component unit is invalid");
        }
        if (guestRoomComponents != 1) fail("Exactly one GUEST_ROOMS component is required");
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private void range(double value, double min, double max, String field) {
        if (!Double.isFinite(value) || value < min || value > max) fail(field + " is outside the accepted range");
    }
    private void componentConsumptionRange(AiHotelSpecification.AiHotelComponentSpecification component, int rooms) {
        double value = component.baseDailyConsumptionLiters();
        double minimum = switch (component.type()) {
            case GUEST_ROOMS -> 150;
            case POOL, LAUNDRY, RESTAURANT, SPA, COOLING_TOWER -> 500;
            case KITCHEN -> component.quantity() == rooms ? 20 : 500;
            case IRRIGATION -> 2;
            case OTHER -> 10;
        };
        double maximum = switch (component.type()) {
            case GUEST_ROOMS -> 500;
            case LAUNDRY -> 10_000;
            case RESTAURANT, POOL -> 15_000;
            case KITCHEN -> component.quantity() == rooms ? 100 : 15_000;
            case SPA, COOLING_TOWER -> 20_000;
            case IRRIGATION -> 15;
            case OTHER -> 100_000;
        };
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            fail("component '" + component.name() + "' has baseDailyConsumptionLiters=" + value
                    + "; expected a per-unit daily value between " + minimum + " and " + maximum
                    + " liters for " + component.type());
        }
        validateUnit(component);
    }

    private void validateUnit(AiHotelSpecification.AiHotelComponentSpecification component) {
        String unit = component.unit().strip().toLowerCase().replace("²", "2");
        Set<String> accepted = switch (component.type()) {
            case GUEST_ROOMS -> Set.of("room", "rooms");
            case POOL -> Set.of("pool", "pools");
            case LAUNDRY -> Set.of("machine", "machines");
            case RESTAURANT -> Set.of("restaurant", "restaurants", "outlet", "outlets");
            case KITCHEN -> Set.of("kitchen", "kitchens");
            case COOLING_TOWER -> Set.of("cooling tower", "cooling towers", "tower", "towers");
            case IRRIGATION -> Set.of("m2", "square meter", "square meters", "square metre", "square metres");
            case SPA -> Set.of("spa", "spas");
            case OTHER -> Set.of(unit);
        };
        if (!accepted.contains(unit)) {
            fail("component '" + component.name() + "' uses unit '" + component.unit()
                    + "'; expected one of " + accepted + " for " + component.type());
        }
    }
    private void fail(String message) { throw new AiAnalysisException("Invalid AI hotel specification: " + message); }
}
