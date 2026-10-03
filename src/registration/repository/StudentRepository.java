package registration.repository;

import registration.model.Student;

public class StudentRepository extends RepositoryBase<Student> {
    @Override protected String entityName() { return "Student"; }
    @Override protected String keyOf(Student s) { return s.getId(); }


    @Override
    protected boolean matches(Student s, String q) {
        return s.getId().equalsIgnoreCase(q)
                || s.getName().toLowerCase().contains(q.toLowerCase());
    }
}
