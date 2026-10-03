package registration.service;

import java.util.List;

import registration.model.Student;
import registration.repository.IRepository;

/** Student management: register, update, view, search. */
public class StudentService {
    private final IRepository<Student> students;

    public StudentService(IRepository<Student> students) {
        this.students = students;
    }

    public Student register(String id, String name, String email, String program) {
        Student student = new Student(id, name, email, program); // constructor validates
        students.add(student);
        return student;
    }

    /** Blank input means "keep the current value". */
    public Student update(String id, String name, String email, String program) {
        Student s = students.getByKey(id);
        if (name != null && !name.trim().isEmpty()) s.setName(name);
        if (email != null && !email.trim().isEmpty()) s.setEmail(email);
        if (program != null && !program.trim().isEmpty()) s.setProgram(program);
        return s;
    }

    public Student get(String id) { return students.getByKey(id); }
    public List<Student> search(String query) { return students.search(query); }
    public List<Student> getAll() { return students.getAll(); }
}
