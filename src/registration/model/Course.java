package registration.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import registration.exception.ValidationException;
import registration.util.Validator;


public class Course {
    private final String code;
    private String title;
    private int capacity;
    private final List<String> enrolledStudentIds = new ArrayList<String>();
    private final List<String> waitlist = new ArrayList<String>(); // index 0 = first in line

    public Course(String code, String title, int capacity) {
        this.code = Validator.requireCourseCode(code);
        setTitle(title);
        setCapacity(capacity);
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCapacity() { return capacity; }

    public void setTitle(String title) { this.title = Validator.requireText(title, "Course title"); }

    public void setCapacity(int capacity) {
        if (capacity < 1 || capacity > 500) {
            throw new ValidationException("Capacity must be between 1 and 500.");
        }
        if (capacity < enrolledStudentIds.size()) {
            throw new ValidationException(
                    "Capacity cannot be lower than current enrolment (" + enrolledStudentIds.size() + ").");
        }
        this.capacity = capacity;
    }

    public List<String> getEnrolledStudentIds() { return Collections.unmodifiableList(enrolledStudentIds); }
    public List<String> getWaitlist() { return Collections.unmodifiableList(waitlist); }

    public boolean isFull() { return enrolledStudentIds.size() >= capacity; }
    public int getSeatsLeft() { return capacity - enrolledStudentIds.size(); }

    public boolean hasStudent(String id) { return indexOf(enrolledStudentIds, id) >= 0; }
    public boolean isOnWaitlist(String id) { return indexOf(waitlist, id) >= 0; }


    public int waitlistPosition(String id) { return indexOf(waitlist, id) + 1; }

    private static int indexOf(List<String> list, String id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(id)) return i;
        }
        return -1;
    }

    public void addStudent(String id) { enrolledStudentIds.add(id); }
    public boolean removeStudent(String id) { return enrolledStudentIds.remove(id); }
    public void addToWaitlist(String id) { waitlist.add(id); }
    public boolean removeFromWaitlist(String id) { return waitlist.remove(id); }
    public void clearAll() { enrolledStudentIds.clear(); waitlist.clear(); }

    @Override
    public String toString() {
        return code + " | " + title + " | " + enrolledStudentIds.size() + "/" + capacity
                + " enrolled | Waitlist: " + waitlist.size();
    }
}
