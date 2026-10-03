package registration.exception;

/** Raised when a student or course lookup fails. */
public class NotFoundException extends RegistrationException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String what, String key) {
        super(what + " '" + key + "' was not found.");
    }
}
