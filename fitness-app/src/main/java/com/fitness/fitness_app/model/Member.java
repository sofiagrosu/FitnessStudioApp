package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fitness.fitness_app.model.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@Entity
@Table(name="members")
public class Member extends User {

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