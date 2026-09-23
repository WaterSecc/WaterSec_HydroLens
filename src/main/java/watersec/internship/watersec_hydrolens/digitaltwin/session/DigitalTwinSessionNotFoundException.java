package watersec.internship.watersec_hydrolens.digitaltwin.session;
import watersec.internship.watersec_hydrolens.common.exception.ResourceNotFoundException;
import java.util.UUID;
public class DigitalTwinSessionNotFoundException extends ResourceNotFoundException {
    public DigitalTwinSessionNotFoundException(UUID id) { super("Digital Twin session was not found or has expired: " + id); }
}
