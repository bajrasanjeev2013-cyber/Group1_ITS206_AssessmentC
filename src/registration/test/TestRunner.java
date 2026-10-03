package registration.test;

import registration.exception.NotFoundException;
import registration.exception.RegistrationException;
import registration.exception.ValidationException;
import registration.model.Person;
import registration.model.Student;
import registration.repository.CourseRepository;
import registration.repository.StudentRepository;
import registration.service.CourseService;
import registration.service.EnrolmentService;
import registration.service.StudentService;
import registration.service.WithdrawResult;


public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    private static class Sys {
        final StudentService students;
        final CourseService courses;
        final EnrolmentService enrol;

        Sys() {
            StudentRepository sr = new StudentRepository();
            CourseRepository cr = new CourseRepository();
            students = new StudentService(sr);
            courses = new CourseService(cr, sr);
            enrol = new EnrolmentService(sr, cr);
        }
    }

    // ---------- tiny test framework ----------
    private static void test(String name, Runnable body) {
        try {
            body.run();
            passed++;
            System.out.println("PASS  " + name);
        } catch (Throwable ex) {
            failed++;
            System.out.println("FAIL  " + name + " -> " + ex.getMessage());
        }
    }

    private static void isTrue(boolean cond) {
        if (!cond) throw new AssertionError("condition was false");
    }

    private static void eq(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("expected " + expected + ", got " + actual);
    }

    private static void throwsEx(Class<? extends Exception> type, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            if (type.isInstance(e)) return;
            throw new AssertionError("expected " + type.getSimpleName() + " but got " + e.getClass().getSimpleName());
        }
        throw new AssertionError("expected " + type.getSimpleName());
    }

    private static Sys withData(int capacity) {
        Sys s = new Sys();
        s.students.register("S001", "Alice", "a@x.com", "BIT");
        s.students.register("S002", "Ben", "b@x.com", "BIT");
        s.students.register("S003", "Cara", "c@x.com", "BIT");
        s.courses.add("ITS206", "Software Construction", capacity);
        return s;
    }

    private static final String[] FOUR = { "ITS101", "ITS102", "ITS103", "ITS104" };

    private static void addCourses(Sys s, String... codes) {
        for (String c : codes) s.courses.add(c, "T", 5);
    }

    public static void main(String[] args) {
        System.out.println("=== Unit tests: Student management ===");
        test("UT01 Register valid student", () -> {
            Sys s = new Sys();
            eq("S001", s.students.register("S001", "Alice", "a@x.com", "BIT").getId());
        });
        test("UT02 Duplicate student ID rejected", () -> {
            Sys s = withData(2);
            throwsEx(RegistrationException.class, () -> s.students.register("s001", "X", "x@x.com", "BIT"));
        });
        test("UT03 Invalid email rejected", () -> {
            Sys s = new Sys();
            throwsEx(ValidationException.class, () -> s.students.register("S001", "A", "bad-email", "BIT"));
        });
        test("UT04 Empty name rejected", () -> {
            Sys s = new Sys();
            throwsEx(ValidationException.class, () -> s.students.register("S001", " ", "a@x.com", "BIT"));
        });
        test("UT05 Invalid ID rejected", () -> {
            Sys s = new Sys();
            throwsEx(ValidationException.class, () -> s.students.register("!!", "A", "a@x.com", "BIT"));
        });
        test("UT06 Update changes only supplied fields", () -> {
            Sys s = withData(2);
            Student u = s.students.update("S001", "Alice B", "", "");
            eq("Alice B", u.getName());
            eq("a@x.com", u.getEmail());
        });
        test("UT07 Search by ID", () -> {
            Sys s = withData(2);
            eq(1, s.students.search("S002").size());
        });
        test("UT08 Search by partial name (case-insensitive)", () -> {
            Sys s = withData(2);
            eq("Cara", s.students.search("car").get(0).getName());
        });
        test("UT09 View unknown student -> NotFound", () -> {
            Sys s = withData(2);
            throwsEx(NotFoundException.class, () -> s.students.get("NOPE1"));
        });
        test("UT10 Polymorphism: getRole via Person reference", () -> {
            Person p = new Student("S009", "Z", "z@x.com", "BIT");
            eq("Student", p.getRole());
        });

        System.out.println("\n=== Unit tests: Course management ===");
        test("UT11 Add course", () -> {
            Sys s = withData(2);
            eq("Software Construction", s.courses.get("its206").getTitle());
        });
        test("UT12 Duplicate course code rejected", () -> {
            Sys s = withData(2);
            throwsEx(RegistrationException.class, () -> s.courses.add("ITS206", "Dup", 5));
        });
        test("UT13 Invalid course code rejected", () -> {
            Sys s = new Sys();
            throwsEx(ValidationException.class, () -> s.courses.add("12", "T", 5));
        });
        test("UT14 Invalid capacity rejected", () -> {
            Sys s = new Sys();
            throwsEx(ValidationException.class, () -> s.courses.add("ITS101", "T", 0));
        });
        test("UT15 Update course title/capacity", () -> {
            Sys s = withData(2);
            s.courses.update("ITS206", "New Title", Integer.valueOf(10));
            eq(10, s.courses.get("ITS206").getCapacity());
        });
        test("UT16 Cannot reduce capacity below enrolment", () -> {
            Sys s = withData(3);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.enrol("S002", "ITS206");
            throwsEx(ValidationException.class, () -> s.courses.update("ITS206", null, Integer.valueOf(1)));
        });
        test("UT17 Search course by code and title", () -> {
            Sys s = withData(2);
            eq(1, s.courses.search("ITS206").size());
            eq(1, s.courses.search("construction").size());
        });
        test("UT18 Remove course clears student enrolments", () -> {
            Sys s = withData(2);
            s.enrol.enrol("S001", "ITS206");
            s.courses.remove("ITS206");
            eq(0, s.students.get("S001").getEnrolledCourseCodes().size());
        });
        test("UT19 Available offerings excludes full courses", () -> {
            Sys s = withData(1);
            s.courses.add("ITS303", "UX", 5);
            s.enrol.enrol("S001", "ITS206");
            eq(1, s.courses.getAvailable().size());
        });

        System.out.println("\n=== Unit tests: Enrolment ===");
        test("UT20 Enrol student", () -> {
            Sys s = withData(2);
            s.enrol.enrol("S001", "ITS206");
            isTrue(s.students.get("S001").isEnrolledIn("ITS206"));
        });
        test("UT21 Duplicate enrolment rejected", () -> {
            Sys s = withData(2);
            s.enrol.enrol("S001", "ITS206");
            throwsEx(RegistrationException.class, () -> s.enrol.enrol("S001", "ITS206"));
        });
        test("UT22 Max 4 courses enforced", () -> {
            Sys s = withData(2);
            addCourses(s, FOUR);
            for (String c : FOUR) s.enrol.enrol("S001", c);
            throwsEx(RegistrationException.class, () -> s.enrol.enrol("S001", "ITS206"));
        });
        test("UT23 Enrol into full course rejected", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            throwsEx(RegistrationException.class, () -> s.enrol.enrol("S002", "ITS206"));
        });
        test("UT24 Withdraw student", () -> {
            Sys s = withData(2);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.withdraw("S001", "ITS206");
            eq(0, s.courses.get("ITS206").getEnrolledStudentIds().size());
        });
        test("UT25 Withdraw when not enrolled rejected", () -> {
            Sys s = withData(2);
            throwsEx(RegistrationException.class, () -> s.enrol.withdraw("S001", "ITS206"));
        });
        test("UT26 Display enrolments", () -> {
            Sys s = withData(2);
            s.enrol.enrol("S001", "ITS206");
            eq(1, s.enrol.getEnrolments("S001").size());
        });
        test("UT27 Enrol unknown student -> NotFound", () -> {
            Sys s = withData(2);
            throwsEx(NotFoundException.class, () -> s.enrol.enrol("NOPE1", "ITS206"));
        });

        System.out.println("\n=== Unit tests: Waitlist extension ===");
        test("UT28 Join waitlist when full", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            eq(1, s.enrol.joinWaitlist("S002", "ITS206"));
        });
        test("UT29 Cannot join waitlist when seats available", () -> {
            Sys s = withData(2);
            throwsEx(RegistrationException.class, () -> s.enrol.joinWaitlist("S001", "ITS206"));
        });
        test("UT30 Duplicate waitlist join rejected", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.joinWaitlist("S002", "ITS206");
            throwsEx(RegistrationException.class, () -> s.enrol.joinWaitlist("S002", "ITS206"));
        });
        test("UT31 Enrolled student cannot join own course's waitlist", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            throwsEx(RegistrationException.class, () -> s.enrol.joinWaitlist("S001", "ITS206"));
        });
        test("UT32 FIFO positions", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.joinWaitlist("S002", "ITS206");
            eq(2, s.enrol.joinWaitlist("S003", "ITS206"));
        });
        test("UT33 Withdraw auto-promotes first in line", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.joinWaitlist("S002", "ITS206");
            s.enrol.joinWaitlist("S003", "ITS206");
            WithdrawResult r = s.enrol.withdraw("S001", "ITS206");
            eq("S002", r.getPromotedStudent().getId());
            isTrue(s.students.get("S002").isEnrolledIn("ITS206"));
            eq(1, s.courses.get("ITS206").getWaitlist().size());
        });
        test("UT34 Promotion skips student already at 4-course limit", () -> {
            Sys s = withData(1);
            addCourses(s, FOUR);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.joinWaitlist("S002", "ITS206");
            s.enrol.joinWaitlist("S003", "ITS206");
            for (String c : FOUR) s.enrol.enrol("S002", c); // S002 now at the 4-course limit
            WithdrawResult r = s.enrol.withdraw("S001", "ITS206");
            eq("S003", r.getPromotedStudent().getId());
            isTrue(s.courses.get("ITS206").isOnWaitlist("S002"));
        });
        test("UT35 Leave waitlist", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.joinWaitlist("S002", "ITS206");
            s.enrol.leaveWaitlist("S002", "ITS206");
            eq(0, s.courses.get("ITS206").getWaitlist().size());
        });
        test("UT36 Leave waitlist when not on it rejected", () -> {
            Sys s = withData(1);
            throwsEx(RegistrationException.class, () -> s.enrol.leaveWaitlist("S002", "ITS206"));
        });
        test("UT37 No promotion when waitlist empty", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            isTrue(s.enrol.withdraw("S001", "ITS206").getPromotedStudent() == null);
        });

        System.out.println("\n=== Integration tests ===");
        test("IT01 Full lifecycle: register -> enrol -> full -> waitlist -> withdraw -> promoted", () -> {
            Sys s = withData(2);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.enrol("S002", "ITS206");
            isTrue(s.courses.get("ITS206").isFull());
            s.enrol.joinWaitlist("S003", "ITS206");
            s.enrol.withdraw("S001", "ITS206");
            isTrue(s.students.get("S003").isEnrolledIn("ITS206"));
            isTrue(s.courses.get("ITS206").isFull());
            eq(0, s.courses.get("ITS206").getWaitlist().size());
        });
        test("IT02 Removing a course clears its waitlist and enrolments", () -> {
            Sys s = withData(1);
            s.enrol.enrol("S001", "ITS206");
            s.enrol.joinWaitlist("S002", "ITS206");
            s.courses.remove("ITS206");
            eq(0, s.students.get("S001").getEnrolledCourseCodes().size());
            throwsEx(NotFoundException.class, () -> s.courses.get("ITS206"));
        });
        test("IT03 Student can hold 4 courses then frees a slot by withdrawing", () -> {
            Sys s = withData(2);
            addCourses(s, "ITS101", "ITS102", "ITS103");
            for (String c : new String[] { "ITS101", "ITS102", "ITS103", "ITS206" }) s.enrol.enrol("S001", c);
            isTrue(s.students.get("S001").isAtLimit());
            s.enrol.withdraw("S001", "ITS103");
            isTrue(!s.students.get("S001").isAtLimit());
        });

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed, " + (passed + failed) + " total");
        if (failed > 0) System.exit(1);
    }
}
