package registration.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import registration.util.Validator;

/**
 * Standard student. INHERITS from {@link Person}.
 * getMaxCourses() is overridable so a subclass can change the limit.
 */
public class Student extends Person {
    public static final int STANDARD_MAX_COURSES = 4;

    private final List<String> enrolledCourseCodes = new ArrayList<String>();
    private String program;

    public Student(String id, String name, String email, String program) {
        super(id, name, email);
        setProgram(program);
    }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = Validator.requireText(program, "Program"); }

    /** Read-only view: callers cannot modify the list directly (encapsulation). */
    public List<String> getEnrolledCourseCodes() {
        return Collections.unmodifiableList(enrolledCourseCodes);
    }

    /** Polymorphic limit. */
    public int getMaxCourses() { return STANDARD_MAX_COURSES; }

    public boolean isAtLimit() { return enrolledCourseCodes.size() >= getMaxCourses(); }

    public boolean isEnrolledIn(String code) {
        for (String c : enrolledCourseCodes) {
            if (c.equalsIgnoreCase(code)) return true;
        }
        return false;
    }

    /** Called by the service layer only. */
    public void addCourse(String code) { enrolledCourseCodes.add(code); }
    public boolean removeCourse(String code) { return enrolledCourseCodes.remove(code); }

    @Override
    public String getRole() { return "Student"; }

    @Override
    public String toString() {
        return super.toString() + " | " + program + " | Enrolled: "
                + enrolledCourseCodes.size() + "/" + getMaxCourses();
    }
}
