# Student Course Registration System (ITS206 Assessment C, Group 1)

Java console application using object-oriented design.
**Personalised extension (Group 1 = odd): Course Waitlist.**

Authors: Sanjeev Bajracharya, Bikash Pandey

## Open in Eclipse
1. **File > Import > General > Existing Projects into Workspace**.
2. Choose **Select root directory** and pick the folder containing this README (the one with `.project`).
3. Tick the project and press **Finish**. (Requires JDK 11 or newer; the default workspace JRE is used.)

## Run the application
Right-click `src/registration/app/Main.java` > **Run As > Java Application**.
Type your answers in the Eclipse **Console** tab. Demo data is preloaded
(students S001-S003; ITS206 is full with capacity 2, so S003 can try the waitlist).

## Run the tests
Right-click `src/registration/test/TestRunner.java` > **Run As > Java Application**.
Prints PASS/FAIL for 40 tests (37 unit + 3 integration). The runner needs no libraries.

## Run from a terminal (optional)
```
mkdir out
javac -d out $(find src -name "*.java")
java -cp out registration.app.Main
java -cp out registration.test.TestRunner
```

## Features
- **Students:** register, update, view profile, search by ID or name
- **Courses:** add, update, remove, search by code or title, list available offerings
- **Enrolment:** enrol, withdraw, display enrolments, max **4** courses per student
- **Waitlist (extension):** join when a course is full (FIFO), leave, view queue;
  when a seat opens the first eligible waitlisted student is auto-enrolled

## Project structure
```
src/registration
  model/        Person (abstract), Student, Course
  repository/   IRepository<T> (interface), RepositoryBase<T> (abstract), StudentRepository, CourseRepository
  service/      StudentService, CourseService, EnrolmentService, WithdrawResult
  exception/    RegistrationException, NotFoundException, ValidationException
  util/         Validator
  app/          Main (console menu)
  test/         TestRunner (unit + integration tests)
```

## OOP concepts
- **Abstraction:** `Person` (abstract class), `IRepository<T>` (interface), `RepositoryBase<T>` (abstract generic class)
- **Inheritance:** `Student extends Person`; `StudentRepository`/`CourseRepository extends RepositoryBase<T>`
- **Encapsulation:** private fields, unmodifiable list views, validated setters
- **Polymorphism:** `Person.getRole()`, overridden `toString()`, `Student.getMaxCourses()`, repository `matches()`/`keyOf()`
