package registration.exception;

/** Raised when a user-supplied value fails validation. */
public class ValidationException extends RegistrationException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
