package com.fitness.fitness_app.repository;

import java.util.List;

import com.fitness.fitness_app.domain.Role;
import com.fitness.fitness_app.domain.UserI;

public interface UserRepositoryI {
List<UserI> getAll();
List<UserI> getByRole(Role role);
UserI getById(Long id);


void add(UserI user);
void update(UserI user);
void delete(Long id);

}
