package registration.repository;

import registration.model.Course;

public class CourseRepository extends RepositoryBase<Course> {
    @Override protected String entityName() { return "Course"; }
    @Override protected String keyOf(Course c) { return c.getCode(); }

    /** Search by course code or (partial, case-insensitive) title. */
    @Override
    protected boolean matches(Course c, String q) {
        return c.getCode().equalsIgnoreCase(q)
                || c.getTitle().toLowerCase().contains(q.toLowerCase());
    }
}
