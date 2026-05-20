package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fitness.fitness_app.model.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@Entity
@Table(name = "receptionists")
public class Receptionist extends User {

    public Receptionist() {
        this.role = Role.RECEPTIONIST;
    }

    public Receptionist(String firstName,
                        String lastName,
                        String email,
                        String password) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;

        this.role = Role.RECEPTIONIST;
    }

    @JsonIgnore
    @Override
    public String getInformations() {
        return "Receptionist: " + getName() + " (" + email + ")";
    }

    @Override
    public String toString() {
        return getInformations();
    }
}