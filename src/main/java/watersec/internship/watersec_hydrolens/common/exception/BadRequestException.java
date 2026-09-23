package watersec.internship.watersec_hydrolens.common.exception;

/**
 * Thrown for semantically invalid requests that pass Bean Validation but violate
 * a business rule (e.g. duplicate answer keys). Maps to HTTP 400 globally.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
