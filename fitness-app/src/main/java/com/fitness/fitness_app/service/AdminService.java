package com.fitness.fitness_app.service;

import com.fitness.fitness_app.model.UserI;
import com.fitness.fitness_app.repository.FileUserRepository;
import java.util.List;
import java.util.stream.Collectors;

public class AdminService {
    private final FileUserRepository userRepo = new FileUserRepository();

    public void addUser(UserI newUser) {
        if (userRepo.findByEmail(newUser.getEmail()) != null) {
            System.out.println("Eroare: Acest email este deja înregistrat!");
            return;
        }
        userRepo.save(newUser);
        System.out.println("Utilizator adăugat cu succes: " + newUser.getInformations()); // Modificat aici
    }

    public void deactivateUser(Long id) {
        userRepo.deleteById(id);
        System.out.println("Utilizatorul cu ID-ul " + id + " a fost dezactivat.");
    }

    public List<UserI> getAllActiveUsers() {
        return userRepo.findAll().stream()
                .filter(UserI::getIsActive) // Modificat aici din .isActive()
                .collect(Collectors.toList());
    }
}