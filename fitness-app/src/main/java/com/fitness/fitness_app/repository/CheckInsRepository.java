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
public class CheckInsRepository implements FileRepository<CheckIn> {
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
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try { return objectMapper.readValue(file, new TypeReference<List<CheckIn>>() {}); }
        catch (IOException e) { throw new RuntimeException("Error reading check-ins from JSON file", e); }
    }

    private Long getNextId() { return checkIns.stream().map(CheckIn::getId).max(Long::compareTo).orElse(0L) + 1; }
    @Override public List<CheckIn> getAll() { return new ArrayList<>(checkIns); }

    @Override
    public void SaveAll(List<CheckIn> items) {
        this.checkIns = new ArrayList<>(items);
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, this.checkIns);
        } catch (IOException e) { throw new RuntimeException("Error saving check-ins to JSON file", e); }
    }

    @Override public void add(CheckIn item) { if (item.getId() == null) item.setId(getNextId()); checkIns.add(item); SaveAll(checkIns); }
    @Override public void update(CheckIn item) { for (int i=0;i<checkIns.size();i++) { if (checkIns.get(i).getId().equals(item.getId())) { checkIns.set(i,item); SaveAll(checkIns); return; } } throw new RuntimeException("Check-in not found"); }
    @Override public void delete(Long id) { boolean removed=checkIns.removeIf(checkIn -> checkIn.getId().equals(id)); if(!removed) throw new RuntimeException("Check-in not found"); SaveAll(checkIns); }
    @Override public CheckIn findById(Long id) { return checkIns.stream().filter(checkIn -> checkIn.getId().equals(id)).findFirst().orElse(null); }
    public List<CheckIn> findOpenByMemberId(Long memberId) { return checkIns.stream().filter(checkIn -> checkIn.getMemberId().equals(memberId) && checkIn.getCheckOutTime() == null).toList(); }
    public List<CheckIn> findOpenByLocationId(Long locationId) { return checkIns.stream().filter(checkIn -> checkIn.getLocationId().equals(locationId) && checkIn.getCheckOutTime() == null).toList(); }
    @Override public List<String> getAllInformation() { return checkIns.stream().map(CheckIn::toString).toList(); }
}
