package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitness.fitness_app.model.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="members")
public class Member extends User {

    private String phone;

    @Column(unique=true)
    private String qrCode;

    public Member(){
        this.role=Role.MEMBER;
        this.active=true;
    }

    public Member(String firstName,
                  String lastName,
                  String email,
                  String phone){

        this.firstName=firstName;
        this.lastName=lastName;
        this.email=email;
        this.phone=phone;

        this.role=Role.MEMBER;
        this.active=true;
    }

    @JsonIgnore
    public String getFullName(){
        return getName();
    }

    @JsonIgnore
    @Override
    public String getInformations(){
        return "Member: "+getName()+" ("+email+")";
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone=phone;
    }

    public String getQrCode(){
        return qrCode;
    }

    public void setQrCode(String qrCode){
        this.qrCode=qrCode;
    }

    @Override
    public String toString(){
        return getInformations();
    }
}