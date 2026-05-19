package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitness.fitness_app.model.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="trainers")
public class Trainer extends User {

    public Trainer() {
        this.role=Role.TRAINER;
        this.active=true;
    }

    public Trainer(String firstName,
                   String lastName,
                   String email,
                   String password){

        this.firstName=firstName;
        this.lastName=lastName;
        this.email=email;
        this.password=password;

        this.role=Role.TRAINER;
        this.active=true;
    }

    @JsonIgnore
    @Override
    public String getInformations() {
        return "Trainer: "+getName()+" ("+email+")";
    }

    @Override
    public String toString() {
        return getInformations();
    }
}