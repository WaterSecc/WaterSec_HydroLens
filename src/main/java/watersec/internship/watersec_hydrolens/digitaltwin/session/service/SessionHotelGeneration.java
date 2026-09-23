package watersec.internship.watersec_hydrolens.digitaltwin.session.service;
import watersec.internship.watersec_hydrolens.ai.dto.AiHotelSpecification;
public record SessionHotelGeneration(AiHotelSpecification specification, long seed, String mode, String model) {}
