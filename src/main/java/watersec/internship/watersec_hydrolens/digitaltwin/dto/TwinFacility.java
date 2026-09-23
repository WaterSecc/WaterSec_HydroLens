package watersec.internship.watersec_hydrolens.digitaltwin.dto;

import java.util.UUID;

public record TwinFacility(
        UUID id,
        String name,
        String type,
        String country,
        String city,
        Integer stars,
        Integer floors,
        Integer constructionYear,
        String primaryWaterSource) {
}
