package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fitness.fitness_app.model.Admin;
import com.fitness.fitness_app.model.Receptionist;
import com.fitness.fitness_app.model.Trainer;
import com.fitness.fitness_app.model.UserI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FileUserRepository implements BaseRepository<UserI> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<UserI> users;

    public FileUserRepository(@Value("${data.users.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.users = loadFromFile();
    }

    private List<UserI> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<UserI>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading users from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, users);
        } catch (IOException e) {
            throw new RuntimeException("Error saving users to JSON file", e);
        }
    }

    private long getNextId() {
        return users.stream().mapToLong(u -> u.getId() == null ? 0L : u.getId()).max().orElse(0L) + 1;
    }

    private void assignIdIfNeeded(UserI user) {
        if (user.getId() != null) return;
        long id = getNextId();
        if (user instanceof Admin admin) admin.setId(id);
        else if (user instanceof Receptionist r) r.setId(id);
        else if (user instanceof Trainer t) t.setId(id);
    }

    @Override
    public UserI save(UserI user) {
        assignIdIfNeeded(user);
        UserI existing = findById(user.getId());
        if (existing != null) users.remove(existing);
        users.add(user);
        saveAll();
        return user;
    }

    @Override
    public UserI findById(Long id) {
        if (id == null) return null;
        return users.stream().filter(u -> id.equals(u.getId())).findFirst().orElse(null);
    }

    @Override
    public List<UserI> findAll() {
        return new ArrayList<>(users);
    }

    @Override
    public void deleteById(Long id) {
        UserI user = findById(id);
        if (user != null) {
            user.setActive(false);
            saveAll();
        }
    }

    public UserI findByEmail(String email) {
        if (email == null) return null;
        return users.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }
}


