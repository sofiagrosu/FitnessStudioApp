package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.fitness_app.model.SignUp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Repository
public class SignUpsRepository implements BaseRepository<SignUp> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<SignUp> signUps;

    public SignUpsRepository(@Value("${data.signups.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.signUps = loadFromFile();
    }

    private List<SignUp> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<SignUp>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading sign-ups from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, signUps);
        } catch (IOException e) {
            throw new RuntimeException("Error saving sign-ups to JSON file", e);
        }
    }

    private Long getNextId() {
        return signUps.stream()
                .map(SignUp::getId)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }

    @Override
    public SignUp save(SignUp entity) {
        if (entity == null) throw new RuntimeException("Sign-up data is required");
        if (entity.getId() == null) entity.setId(getNextId());
        if (entity.getBookingTime() == null) throw new RuntimeException("Booking time is required");

        for (int i = 0; i < signUps.size(); i++) {
            Long existingId = signUps.get(i).getId();
            if (existingId != null && existingId.equals(entity.getId())) {
                signUps.set(i, entity);
                saveAll();
                return entity;
            }
        }
        signUps.add(entity);
        saveAll();
        return entity;
    }

    @Override
    public SignUp findById(Long id) {
        if (id == null) return null;
        return signUps.stream()
                .filter(s -> id.equals(s.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<SignUp> findAll() {
        return new ArrayList<>(signUps);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("Sign-up id is required");
        boolean removed = signUps.removeIf(s -> id.equals(s.getId()));
        if (!removed) throw new RuntimeException("Sign-up not found");
        saveAll();
    }

    public List<SignUp> findByCourseId(Long courseId) {
        if (courseId == null) return List.of();
        return signUps.stream().filter(s -> Long.valueOf(s.getCourseId()).equals(courseId)).toList();
    }

    public List<SignUp> findByMemberId(Long memberId) {
        if (memberId == null) return List.of();
        return signUps.stream().filter(s -> Long.valueOf(s.getMemberId()).equals(memberId)).toList();
    }

    public List<SignUp> findAttended() {
        return signUps.stream().filter(s -> Boolean.TRUE.equals(s.getAttended())).toList();
    }

    public List<SignUp> findNotAttended() {
        return signUps.stream().filter(s -> !Boolean.TRUE.equals(s.getAttended())).toList();
    }

    public SignUp findByCourseIdAndMemberId(Long courseId, Long memberId) {
        if (courseId == null || memberId == null) return null;
        return signUps.stream()
                .filter(s -> Long.valueOf(s.getCourseId()).equals(courseId)
                        && Long.valueOf(s.getMemberId()).equals(memberId))
                .findFirst().orElse(null);
    }

    public List<SignUp> sortByBookingTime() {
        return signUps.stream()
                .sorted(Comparator.comparing(SignUp::getBookingTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<SignUp> sortByCourseId() {
        return signUps.stream().sorted(Comparator.comparing(SignUp::getCourseId)).toList();
    }

    public List<SignUp> sortByMemberId() {
        return signUps.stream().sorted(Comparator.comparing(SignUp::getMemberId)).toList();
    }
}
