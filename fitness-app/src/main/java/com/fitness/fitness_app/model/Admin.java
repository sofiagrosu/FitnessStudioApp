package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fitness.fitness_app.model.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@Entity
@Table(name="admins")
public class Admin extends User {

    public Admin() {
        this.role=Role.ADMIN;
        this.active=true;
    }

    public Admin(String email,String password,
                 String firstName,String lastName){

        this.email=email;
        this.password=password;
        this.firstName=firstName;
        this.lastName=lastName;

        this.role=Role.ADMIN;
        this.active=true;
    }

    @JsonIgnore
    @Override
    public String getInformations() {
        return "Admin: " + getName()+" ("+email+")";
    }

    @Override
    public String toString() {
        return getInformations();
    }
}