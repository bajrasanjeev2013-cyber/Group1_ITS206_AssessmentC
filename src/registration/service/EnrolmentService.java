package registration.service;

import java.util.ArrayList;
import java.util.List;

import registration.exception.RegistrationException;
import registration.model.Course;
import registration.model.Student;
import registration.repository.IRepository;

/**
 * Enrolment operations + the Course Waitlist extension (Group 1 = odd number).
 * Rules: max 4 courses per student; full courses accept waitlist joins (FIFO);
 * when a seat opens, the first eligible waitlisted student is auto-enrolled.
 */
public class EnrolmentService {
    private final IRepository<Student> students;
    private final IRepository<Course> courses;

    public EnrolmentService(IRepository<Student> students, IRepository<Course> courses) {
        this.students = students;
        this.courses = courses;
    }

    public void enrol(String studentId, String courseCode) {
        Student s = students.getByKey(studentId);
        Course c = courses.getByKey(courseCode);

        if (s.isEnrolledIn(c.getCode())) {
            throw new RegistrationException(s.getName() + " is already enrolled in " + c.getCode() + ".");
        }
        if (s.isAtLimit()) {
            throw new RegistrationException(
                    s.getName() + " has reached the maximum of " + s.getMaxCourses() + " courses.");
        }
        if (c.isFull()) {
            throw new RegistrationException(c.getCode() + " is full (" + c.getCapacity() + "/"
                    + c.getCapacity() + "). You can join the waitlist instead.");
        }

        c.removeFromWaitlist(s.getId()); // harmless if not on it
        c.addStudent(s.getId());
        s.addCourse(c.getCode());
    }

    public WithdrawResult withdraw(String studentId, String courseCode) {
        Student s = students.getByKey(studentId);
        Course c = courses.getByKey(courseCode);

        if (!s.isEnrolledIn(c.getCode())) {
            throw new RegistrationException(s.getName() + " is not enrolled in " + c.getCode() + ".");
        }
        c.removeStudent(s.getId());
        s.removeCourse(c.getCode());
        Student promoted = promoteFromWaitlist(c);
        return new WithdrawResult(s, c, promoted);
    }

    // ---------- Waitlist extension ----------

    /** Join the waitlist: only allowed when the course is at maximum capacity. Returns position. */
    public int joinWaitlist(String studentId, String courseCode) {
        Student s = students.getByKey(studentId);
        Course c = courses.getByKey(courseCode);

        if (!c.isFull()) {
            throw new RegistrationException(
                    c.getCode() + " still has " + c.getSeatsLeft() + " seat(s) - enrol directly.");
        }
        if (s.isEnrolledIn(c.getCode())) {
            throw new RegistrationException(s.getName() + " is already enrolled in " + c.getCode() + ".");
        }
        if (c.isOnWaitlist(s.getId())) {
            throw new RegistrationException(s.getName() + " is already on the " + c.getCode() + " waitlist.");
        }
        if (s.isAtLimit()) {
            throw new RegistrationException(s.getName() + " has reached the maximum of "
                    + s.getMaxCourses() + " courses and cannot join a waitlist.");
        }
        c.addToWaitlist(s.getId());
        return c.waitlistPosition(s.getId());
    }

    public void leaveWaitlist(String studentId, String courseCode) {
        Student s = students.getByKey(studentId);
        Course c = courses.getByKey(courseCode);
        if (!c.removeFromWaitlist(s.getId())) {
            throw new RegistrationException(s.getName() + " is not on the " + c.getCode() + " waitlist.");
        }
    }

    public List<Student> getWaitlist(String courseCode) {
        Course c = courses.getByKey(courseCode);
        List<Student> result = new ArrayList<Student>();
        for (String id : c.getWaitlist()) {
            if (students.exists(id)) result.add(students.getByKey(id));
        }
        return result;
    }

    /**
     * Fills one open seat from the front of the queue. Students who have since hit the
     * course limit are skipped (and keep their place) so one blocked student never stalls the queue.
     */
    private Student promoteFromWaitlist(Course c) {
        if (c.isFull()) return null;
        for (String id : new ArrayList<String>(c.getWaitlist())) {
            if (!students.exists(id)) {
                c.removeFromWaitlist(id);
                continue;
            }
            Student candidate = students.getByKey(id);
            if (candidate.isAtLimit() || candidate.isEnrolledIn(c.getCode())) continue;

            c.removeFromWaitlist(id);
            c.addStudent(candidate.getId());
            candidate.addCourse(c.getCode());
            return candidate;
        }
        return null;
    }

    // ---------- Reporting ----------

    public List<Course> getEnrolments(String studentId) {
        Student s = students.getByKey(studentId);
        List<Course> result = new ArrayList<Course>();
        for (String code : s.getEnrolledCourseCodes()) result.add(courses.getByKey(code));
        return result;
    }
}
