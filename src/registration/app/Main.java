package registration.app;

import java.util.List;
import java.util.Scanner;

import registration.exception.RegistrationException;
import registration.exception.ValidationException;
import registration.model.Course;
import registration.model.Student;
import registration.repository.CourseRepository;
import registration.repository.StudentRepository;
import registration.service.CourseService;
import registration.service.EnrolmentService;
import registration.service.StudentService;
import registration.service.WithdrawResult;

/**
 * Console UI. All business rules live in the services; this class only reads input and prints.
 * Run this class as a "Java Application" in Eclipse.
 */
public class Main {
    private static final Scanner IN = new Scanner(System.in);
    private static StudentService students;
    private static CourseService courses;
    private static EnrolmentService enrol;

    /** Thrown when the input stream ends (for example when input is piped). */
    private static class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private interface MenuAction { void run(); }

    public static void main(String[] args) {
        // Composition root: wire concrete repositories into services through the IRepository interface.
        StudentRepository studentRepo = new StudentRepository();
        CourseRepository courseRepo = new CourseRepository();
        students = new StudentService(studentRepo);
        courses = new CourseService(courseRepo, studentRepo);
        enrol = new EnrolmentService(studentRepo, courseRepo);

        seedDemoData();

        try {
            while (true) {
                System.out.println("\n===== Student Course Registration System =====");
                System.out.println("1) Student management");
                System.out.println("2) Course management");
                System.out.println("3) Enrolment operations");
                System.out.println("4) Course waitlist");
                System.out.println("0) Exit");
                String choice = ask("Choose");
                if (choice.equals("1")) run(new MenuAction() { public void run() { studentMenu(); } });
                else if (choice.equals("2")) run(new MenuAction() { public void run() { courseMenu(); } });
                else if (choice.equals("3")) run(new MenuAction() { public void run() { enrolmentMenu(); } });
                else if (choice.equals("4")) run(new MenuAction() { public void run() { waitlistMenu(); } });
                else if (choice.equals("0")) break;
                else System.out.println("Invalid option.");
            }
        } catch (EndOfInput e) {
            // input finished: fall through and exit
        }
        System.out.println("Goodbye.");
    }

    /** Global error handling: business/validation errors are shown, never crash the app. */
    private static void run(MenuAction menu) {
        try {
            menu.run();
        } catch (RegistrationException ex) {
            System.out.println("[Error] " + ex.getMessage());
        }
    }

    private static void studentMenu() {
        System.out.println("a) Register  b) Update  c) View profile  d) Search  e) List all");
        String c = ask("Choose").toLowerCase();
        if (c.equals("a")) {
            Student s = students.register(ask("Student ID"), ask("Name"), ask("Email"), ask("Program"));
            System.out.println("Registered: " + s);
        } else if (c.equals("b")) {
            Student u = students.update(ask("Student ID"), ask("New name (blank = keep)"),
                    ask("New email (blank = keep)"), ask("New program (blank = keep)"));
            System.out.println("Updated: " + u);
        } else if (c.equals("c")) {
            printProfile(students.get(ask("Student ID")));
        } else if (c.equals("d")) {
            printList(students.search(ask("ID or name")));
        } else if (c.equals("e")) {
            printList(students.getAll());
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void courseMenu() {
        System.out.println("a) Add  b) Update  c) Remove  d) Search  e) Available offerings  f) List all");
        String c = ask("Choose").toLowerCase();
        if (c.equals("a")) {
            Course course = courses.add(ask("Course code"), ask("Title"), askInt("Capacity"));
            System.out.println("Added: " + course);
        } else if (c.equals("b")) {
            String code = ask("Course code");
            String title = ask("New title (blank = keep)");
            String capText = ask("New capacity (blank = keep)");
            Integer cap = capText.trim().isEmpty() ? null : Integer.valueOf(parseInt(capText, "Capacity"));
            System.out.println("Updated: " + courses.update(code, title, cap));
        } else if (c.equals("c")) {
            courses.remove(ask("Course code"));
            System.out.println("Course removed (students and waitlist cleared).");
        } else if (c.equals("d")) {
            printList(courses.search(ask("Code or title")));
        } else if (c.equals("e")) {
            printList(courses.getAvailable());
        } else if (c.equals("f")) {
            printList(courses.getAll());
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void enrolmentMenu() {
        System.out.println("a) Enrol  b) Withdraw  c) Display current enrolments");
        String c = ask("Choose").toLowerCase();
        if (c.equals("a")) {
            enrol.enrol(ask("Student ID"), ask("Course code"));
            System.out.println("Enrolment successful.");
        } else if (c.equals("b")) {
            WithdrawResult r = enrol.withdraw(ask("Student ID"), ask("Course code"));
            System.out.println("Withdrawn.");
            if (r.getPromotedStudent() != null) {
                System.out.println("Seat given to waitlisted student " + r.getPromotedStudent().getName()
                        + " (" + r.getPromotedStudent().getId() + ").");
            }
        } else if (c.equals("c")) {
            String id = ask("Student ID");
            List<Course> list = enrol.getEnrolments(id);
            System.out.println("Enrolments for " + students.get(id).getName() + ":");
            printList(list);
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void waitlistMenu() {
        System.out.println("a) Join waitlist  b) Leave waitlist  c) View waitlist for a course");
        String c = ask("Choose").toLowerCase();
        if (c.equals("a")) {
            int pos = enrol.joinWaitlist(ask("Student ID"), ask("Course code"));
            System.out.println("Added to waitlist at position " + pos + ".");
        } else if (c.equals("b")) {
            enrol.leaveWaitlist(ask("Student ID"), ask("Course code"));
            System.out.println("Removed from waitlist.");
        } else if (c.equals("c")) {
            String code = ask("Course code");
            List<Student> w = enrol.getWaitlist(code);
            System.out.println("Waitlist for " + code.toUpperCase() + " (" + w.size() + "):");
            for (int i = 0; i < w.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + w.get(i).getName() + " (" + w.get(i).getId() + ")");
            }
        } else {
            System.out.println("Invalid option.");
        }
    }

    // ---------- helpers ----------
    private static String ask(String prompt) {
        System.out.print(prompt + ": ");
        if (!IN.hasNextLine()) throw new EndOfInput();
        return IN.nextLine();
    }

    private static int askInt(String prompt) {
        return parseInt(ask(prompt), prompt);
    }

    private static int parseInt(String text, String field) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a whole number.");
        }
    }

    private static <T> void printList(List<T> items) {
        if (items.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        for (T i : items) System.out.println("  " + i);
    }

    private static void printProfile(Student s) {
        System.out.println("ID: " + s.getId() + "\nName: " + s.getName() + "\nEmail: " + s.getEmail()
                + "\nProgram: " + s.getProgram());
        System.out.println("Enrolled (" + s.getEnrolledCourseCodes().size() + "/" + s.getMaxCourses() + "): "
                + String.join(", ", s.getEnrolledCourseCodes()));
    }

    private static void seedDemoData() {
        students.register("S001", "Alice Smith", "alice@example.com", "BIT");
        students.register("S002", "Ben Jones", "ben@example.com", "BIT");
        students.register("S003", "Cara Lee", "cara@example.com", "BIT");
        courses.add("ITS206", "Software Construction and Design", 2);
        courses.add("ITS303", "User Experience", 30);
        enrol.enrol("S001", "ITS206");
        enrol.enrol("S002", "ITS206"); // ITS206 now full -> demo the waitlist with S003
        System.out.println("Demo data loaded: students S001-S003, courses ITS206 (full, cap 2) and ITS303.");
    }
}
