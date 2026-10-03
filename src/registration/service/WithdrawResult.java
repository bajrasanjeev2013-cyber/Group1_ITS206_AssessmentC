package registration.service;

import registration.model.Course;
import registration.model.Student;

public class WithdrawResult {
    private final Student student;
    private final Course course;
    private final Student promotedStudent;

    public WithdrawResult(Student student, Course course, Student promotedStudent) {
        this.student = student;
        this.course = course;
        this.promotedStudent = promotedStudent;
    }

    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
    /** May be null when nobody was promoted. */
    public Student getPromotedStudent() { return promotedStudent; }
}
