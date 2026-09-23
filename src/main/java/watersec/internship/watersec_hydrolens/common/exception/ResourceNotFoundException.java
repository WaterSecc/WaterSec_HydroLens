package watersec.internship.watersec_hydrolens.common.exception;

/**
 * Base exception for any domain resource that could not be located.
 * Module-specific exceptions (e.g. {@code FacilityNotFoundException}) extend this
 * so the global handler can map the whole family to HTTP 404 in one place.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
