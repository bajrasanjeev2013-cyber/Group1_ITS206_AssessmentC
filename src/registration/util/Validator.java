package registration.util;

import java.util.regex.Pattern;

import registration.exception.ValidationException;


public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern ID = Pattern.compile("^[A-Za-z0-9]{3,12}$");
    private static final Pattern CODE = Pattern.compile("^[A-Za-z]{2,4}\\d{3}$");

    private Validator() { }

    public static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " cannot be empty.");
        }
        value = value.trim();
        if (value.length() > 100) {
            throw new ValidationException(field + " is too long (max 100 characters).");
        }
        return value;
    }

    public static String requireEmail(String value) {
        value = requireText(value, "Email");
        if (!EMAIL.matcher(value).matches()) {
            throw new ValidationException("Email format is invalid (e.g. name@example.com).");
        }
        return value;
    }

    public static String requireId(String value, String field) {
        value = requireText(value, field);
        if (!ID.matcher(value).matches()) {
            throw new ValidationException(field + " must be 3-12 letters/digits.");
        }
        return value.toUpperCase();
    }

    public static String requireCourseCode(String value) {
        value = requireText(value, "Course code");
        if (!CODE.matcher(value).matches()) {
            throw new ValidationException("Course code must look like ITS206 (2-4 letters + 3 digits).");
        }
        return value.toUpperCase();
    }
}
