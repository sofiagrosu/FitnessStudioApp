package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fitness.fitness_app.model.enums.Role;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member implements UserI {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String qrCode;
    private boolean active;
    private Role role;

    public Member() {
        this.active = true;
        this.role = Role.MEMBER;
    }

    public Member(Long id, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.active = true;
        this.role = Role.MEMBER;
    }

    @Override
    public String toString() {
        return "Member{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", active=" + active +
                '}';
    }

    @JsonIgnore
    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }

    @JsonIgnore
    @Override
    public String getName() { return getFullName(); }

    @JsonIgnore
    @Override
    public String getInformations() {
        return "Member: " + getName() + " (" + email + ")";
    }

    @Override public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override public void setActive(boolean active) { this.active = active; }
    @Override public boolean isActive() { return active; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}
