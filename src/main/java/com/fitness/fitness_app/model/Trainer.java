package com.fitness.fitness_app.model;

import com.fitness.fitness_app.model.enums.Role;

public class Trainer implements UserI {
    private static final long serialVersionUID = 1L;

    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean active;
    private Role role;

    public Trainer() {
        this.role = Role.TRAINER;
        this.active = true;
    }

    public Trainer(long id, String firstName, String lastName, String email, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.active = true;
        this.role = Role.TRAINER;
    }

    @Override public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @Override public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String getName() { return firstName + " " + lastName; }

    @Override public Boolean getIsActive() { return active; }

    @Override
    public String getInformations() {
        return "Trainer: " + getName() + " (" + email + ")";
    }

    @Override public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    @Override public String toString() { return getInformations(); }
}