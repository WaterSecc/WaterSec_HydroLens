package watersec.internship.watersec_hydrolens.digitaltwin.session;
import java.time.Instant;
public record SessionGenerationMetadata(String mode, String model, String promptProfile,
                                        String contractVersion, long seed, Instant generatedAt) {}
