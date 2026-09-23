package watersec.internship.watersec_hydrolens.digitaltwin.session.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
public record CreateDigitalTwinSessionRequest(@NotNull HotelArchetype archetype,
        @Size(max = 150) String name, @Size(max = 100) String country,
        @Size(max = 100) String city, Long seed) {}
