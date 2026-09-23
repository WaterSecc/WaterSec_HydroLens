package watersec.internship.watersec_hydrolens.ai.dto;

import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;

import java.util.List;

/**
 * AI-produced configuration only. Consumption and leak totals are deliberately
 * absent: the deterministic Java engines calculate them from this specification.
 */
public record AiHotelSpecification(
        String hotelName,
        String country,
        String city,
        HotelArchetype archetype,
        int stars,
        int floors,
        int rooms,
        double occupancyRate,
        double waterCostPerLiter,
        List<AiHotelComponentSpecification> components,
        List<String> assumptions) {

    public record AiHotelComponentSpecification(
            String name,
            ComponentType type,
            int quantity,
            double baseDailyConsumptionLiters,
            String unit,
            boolean occupancyDependent,
            double efficiencyRating) {
    }
}
