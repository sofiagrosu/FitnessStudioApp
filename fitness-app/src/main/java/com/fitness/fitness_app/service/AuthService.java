package com.fitness.fitness_app.service;

import com.fitness.fitness_app.model.UserI;
import com.fitness.fitness_app.repository.FileUserRepository;

public class AuthService {
    private final FileUserRepository userRepo = new FileUserRepository();

    public UserI login(String email, String password) {
        UserI user = userRepo.findByEmail(email);
        if (user != null && user.getPassword().equals(password) && user.getIsActive()) {
            System.out.println("Login reușit pentru rolul: " + user.getRole());
            return user;
        }
        System.out.println("Email sau parolă incorectă / Cont inactiv.");
        return null;
    }
}