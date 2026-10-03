package registration.service;

import java.util.ArrayList;
import java.util.List;

import registration.model.Course;
import registration.model.Student;
import registration.repository.IRepository;


public class CourseService {
    private final IRepository<Course> courses;
    private final IRepository<Student> students;

    public CourseService(IRepository<Course> courses, IRepository<Student> students) {
        this.courses = courses;
        this.students = students;
    }

    public Course add(String code, String title, int capacity) {
        Course course = new Course(code, title, capacity);
        courses.add(course);
        return course;
    }


    public Course update(String code, String title, Integer capacity) {
        Course c = courses.getByKey(code);
        if (title != null && !title.trim().isEmpty()) c.setTitle(title);
        if (capacity != null) c.setCapacity(capacity.intValue());
        return c;
    }


    public void remove(String code) {
        Course c = courses.getByKey(code);
        for (String id : new ArrayList<String>(c.getEnrolledStudentIds())) {
            if (students.exists(id)) students.getByKey(id).removeCourse(c.getCode());
        }
        c.clearAll();
        courses.remove(code);
    }

    public Course get(String code) { return courses.getByKey(code); }
    public List<Course> search(String query) { return courses.search(query); }
    public List<Course> getAll() { return courses.getAll(); }


    public List<Course> getAvailable() {
        List<Course> result = new ArrayList<Course>();
        for (Course c : courses.getAll()) {
            if (!c.isFull()) result.add(c);
        }
        return result;
    }
}
