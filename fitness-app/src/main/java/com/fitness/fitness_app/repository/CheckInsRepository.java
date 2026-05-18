package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.fitness_app.model.CheckIn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CheckInsRepository implements BaseRepository<CheckIn> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<CheckIn> checkIns;

    public CheckInsRepository(@Value("${data.checkins.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.checkIns = loadFromFile();
    }

    private List<CheckIn> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<CheckIn>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading check-ins from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, checkIns);
        } catch (IOException e) {
            throw new RuntimeException("Error saving check-ins to JSON file", e);
        }
    }

    private Long getNextId() {
        return checkIns.stream()
                .map(CheckIn::getId)
                .filter(id -> id != null)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }

    @Override
    public CheckIn save(CheckIn entity) {
        if (entity.getId() == null) entity.setId(getNextId());
        for (int i = 0; i < checkIns.size(); i++) {
            if (checkIns.get(i).getId().equals(entity.getId())) {
                checkIns.set(i, entity);
                saveAll();
                return entity;
            }
        }
        checkIns.add(entity);
        saveAll();
        return entity;
    }

    @Override
    public CheckIn findById(Long id) {
        if (id == null) return null;
        return checkIns.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<CheckIn> findAll() {
        return new ArrayList<>(checkIns);
    }

    @Override
    public void deleteById(Long id) {
        boolean removed = checkIns.removeIf(c -> c.getId().equals(id));
        if (!removed) throw new RuntimeException("Check-in not found");
        saveAll();
    }

    public List<CheckIn> findOpenByMemberId(Long memberId) {
        return checkIns.stream()
                .filter(c -> c.getMemberId().equals(memberId) && c.getCheckOutTime() == null)
                .toList();
    }

    public List<CheckIn> findAllByMemberId(Long memberId) {
        return checkIns.stream().filter(c -> c.getMemberId().equals(memberId)).toList();
    }

    public List<CheckIn> findOpenByLocationId(Long locationId) {
        return checkIns.stream()
                .filter(c -> c.getLocationId().equals(locationId) && c.getCheckOutTime() == null)
                .toList();
    }
}


