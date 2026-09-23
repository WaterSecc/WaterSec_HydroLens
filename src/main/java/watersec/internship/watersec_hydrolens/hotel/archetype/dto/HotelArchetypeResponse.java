package watersec.internship.watersec_hydrolens.hotel.archetype.dto;

import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;

public record HotelArchetypeResponse(
        HotelArchetype code,
        String displayName,
        String description) {
}
