package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.UserI;
import java.util.List;

public class FileUserRepository implements RepositoryI<UserI> {
    private final DataContext context = DataContext.getInstance();

    @Override
    public void save(UserI user) {
        UserI existing = findById(user.getId());
        if (existing != null) {
            context.getUsers().remove(existing);
        }
        context.getUsers().add(user);
        context.saveAllData();
    }

    @Override
    public UserI findById(Long id) {
        return context.getUsers().stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public UserI findByEmail(String email) {
        return context.getUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<UserI> findAll() {
        return context.getUsers();
    }

    @Override
    public void deleteById(Long id) {
        UserI user = findById(id);
        if (user != null) {
            user.setActive(false);
            context.saveAllData();
        }
    }
}