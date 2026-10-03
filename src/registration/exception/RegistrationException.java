package registration.exception;

/**
 * Base exception for all business-rule failures (duplicate IDs, full courses,
 * enrolment limit reached, etc.). The menu catches this type and prints the message.
 */
public class RegistrationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegistrationException(String message) {
        super(message);
    }
}
