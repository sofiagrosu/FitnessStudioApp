package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitness.fitness_app.model.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User implements UserI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true, nullable = false)
    protected String email;

    protected String password;

    @Enumerated(EnumType.STRING)
    protected Role role;

    protected boolean active=true;

    protected String firstName;

    protected String lastName;

    public User(){}

    @Override
    public Long getId() {
        return id;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return firstName + " " + lastName;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setActive(boolean active) {
        this.active=active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setEmail(String email){
        this.email=email;
    }

    public void setPassword(String password){
        this.password=password;
    }

    public String getFirstName(){
        return firstName;
    }

    public void setFirstName(String firstName){
        this.firstName=firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public void setLastName(String lastName){
        this.lastName=lastName;
    }

    public void setRole(Role role){
        this.role=role;
    }
}