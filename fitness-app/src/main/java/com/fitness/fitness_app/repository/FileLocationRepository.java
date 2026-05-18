package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fitness.fitness_app.model.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FileLocationRepository implements BaseRepository<Location> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<Location> locations;

    public FileLocationRepository(@Value("${data.locations.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.locations = loadFromFile();
    }

    private List<Location> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Location>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading locations from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, locations);
        } catch (IOException e) {
            throw new RuntimeException("Error saving locations to JSON file", e);
        }
    }

    private long getNextId() {
        return locations.stream().map(Location::getId).mapToLong(Long::longValue).max().orElse(0L) + 1;
    }

    @Override
    public Location save(Location location) {
        if (location.getId() == null) location.setId(getNextId());
        Location existing = findById(location.getId());
        if (existing != null) locations.remove(existing);
        locations.add(location);
        saveAll();
        return location;
    }

    @Override
    public Location findById(Long id) {
        if (id == null) return null;
        return locations.stream().filter(l -> l.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Location> findAll() {
        return new ArrayList<>(locations);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("Location id is required");
        Location location = findById(id);
        if (location == null) throw new RuntimeException("Location not found");
        locations.remove(location);
        saveAll();
    }
}


