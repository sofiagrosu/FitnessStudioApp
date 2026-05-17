package com.fitness.fitness_app.domain;

public interface UserI {
 long getId();
 Role getRole();// returneaza rolul utilizatorului (enum)
 String getName();
 Boolean getIsActive();
 String getInformations(); // returneaza un string cu numele si emailul utilizatorului
}
