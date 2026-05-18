package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fitness.fitness_app.model.enums.Role;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Receptionist implements UserI {
    private static final long serialVersionUID = 1L;

    private long id;
    private String email;
    private String password;
    private Role role;
    private boolean active;
    private String firstName;
    private String lastName;

    public Receptionist() {
        this.role = Role.RECEPTIONIST;
        this.active = true;
    }

    public Receptionist(long id, String email, String password, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = Role.RECEPTIONIST;
        this.active = true;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @Override public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @JsonIgnore
    @Override
    public String getName() { return firstName + " " + lastName; }

    @JsonIgnore
    @Override
    public String getInformations() {
        return "Receptioner: " + getName() + " (" + email + ")";
    }

    @Override public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override public void setActive(boolean active) { this.active = active; }
    @Override public boolean isActive() { return active; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    @Override public String toString() { return getInformations(); }
}