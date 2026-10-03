package registration.model;

import registration.util.Validator;


public abstract class Person {
    private final String id;
    private String name;
    private String email;

    protected Person(String id, String name, String email) {
        this.id = Validator.requireId(id, "Student ID");
        setName(name);
        setEmail(email);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = Validator.requireText(name, "Name"); }
    public void setEmail(String email) { this.email = Validator.requireEmail(email); }


    public abstract String getRole();

    @Override
    public String toString() {
        return id + " | " + name + " | " + email + " | " + getRole();
    }
}
