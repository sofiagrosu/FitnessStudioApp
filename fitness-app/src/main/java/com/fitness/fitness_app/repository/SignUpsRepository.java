package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.fitness_app.model.SignUp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SignUpsRepository implements FileRepository<SignUp> {

    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<SignUp> signUps;

    public SignUpsRepository(@Value("${data.signups.path}")     String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.signUps = loadFromFile();
    }

    private List<SignUp> loadFromFile() {
        File file = new File(filePath);

        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    file,
                    new TypeReference<List<SignUp>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error reading sign-ups from JSON file", e
            );
        }
    }

    @Override
    public List<SignUp> getAll() {
        return new ArrayList<>(signUps);
    }

    @Override
    public void SaveAll(List<SignUp> items) {
        this.signUps = new ArrayList<>(items);

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(filePath), this.signUps);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error saving sign-ups to JSON file", e
            );
        }
    }

    @Override
    public void add(SignUp item) {
        signUps.add(item);
        SaveAll(signUps);
    }

    @Override
    public void update(SignUp item) {
        for (int i = 0; i < signUps.size(); i++) {
            if (signUps.get(i).getId().equals(item.getId())) {
                signUps.set(i, item);
                SaveAll(signUps);
                return;
            }
        }

        throw new RuntimeException("Sign-up not found");
    }

    @Override
    public void delete(Long id) {
        boolean removed = signUps.removeIf(
                signUp -> signUp.getId().equals(id)
        );

        if (!removed) {
            throw new RuntimeException("Sign-up not found");
        }

        SaveAll(signUps);
    }

    // getAllInformation
    public List<String> getAllInformation() {
        List<String> infoList = new ArrayList<>();

        for (SignUp signUp : signUps) {
            infoList.add(signUp.toString());
        }

        return infoList;
    }
 //find by id
    public SignUp findById(Long id) {
        return signUps.stream()
                .filter(signUp -> signUp.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
public Long getNextId() {
    return signUps.stream()
            .map(SignUp::getId)
            .max(Long::compareTo)
            .orElse(0L) + 1;
}
    // Filters

    public List<SignUp> findByCourseId(Long courseId) {
        return signUps.stream()
                .filter(signUp -> signUp.getCourseId() == courseId)
                .toList();
    }

    public List<SignUp> findByMemberId(Long memberId) {
        return signUps.stream()
                .filter(signUp -> signUp.getMemberId() == memberId)
                .toList();
    }

    public List<SignUp> findAttended() {
        return signUps.stream()
                .filter(SignUp::getAttended)
                .toList();
    }

    public List<SignUp> findNotAttended() {
        return signUps.stream()
                .filter(signUp -> !signUp.getAttended())
                .toList();
    }

    public SignUp findByCourseIdAndMemberId(Long courseId, Long memberId) {
        return signUps.stream()
                .filter(signUp -> signUp.getCourseId() == courseId)
                .filter(signUp -> signUp.getMemberId() == memberId)
                .findFirst()
                .orElse(null);
    }

    // Sorting

    public List<SignUp> sortByBookingTime() {
        return signUps.stream()
                .sorted(Comparator.comparing(SignUp::getBookingTime))
                .toList();
    }

    public List<SignUp> sortByCourseId() {
        return signUps.stream()
                .sorted(Comparator.comparing(SignUp::getCourseId))
                .toList();
    }

    public List<SignUp> sortByMemberId() {
        return signUps.stream()
                .sorted(Comparator.comparing(SignUp::getMemberId))
                .toList();
    }
}