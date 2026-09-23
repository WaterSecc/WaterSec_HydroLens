package watersec.internship.watersec_hydrolens.hotel.archetype.generator;

import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
import watersec.internship.watersec_hydrolens.hotel.archetype.OperatingProfile;

import java.util.List;

public record SyntheticHotelPlan(
        HotelArchetype archetype,
        long seed,
        String name,
        String country,
        String city,
        int stars,
        int floors,
        int constructionYear,
        double occupancyRate,
        double waterCostPerLiter,
        int employeeCount,
        OperatingProfile operatingProfile,
        List<SyntheticComponentPlan> components) {
}
