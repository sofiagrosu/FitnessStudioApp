package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.fitness_app.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PaymentsRepository implements BaseRepository<Payment> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<Payment> payments;

    public PaymentsRepository(@Value("${data.payments.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.payments = loadFromFile();
    }

    private List<Payment> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Payment>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading payments from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, payments);
        } catch (IOException e) {
            throw new RuntimeException("Error saving payments to JSON file", e);
        }
    }

    private Long getNextId() {
        return payments.stream().map(Payment::getId).mapToLong(Long::longValue).max().orElse(0L) + 1;
    }

    @Override
    public Payment save(Payment entity) {
        if (entity.getId() == null) entity.setId(getNextId());
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getId().equals(entity.getId())) {
                payments.set(i, entity);
                saveAll();
                return entity;
            }
        }
        payments.add(entity);
        saveAll();
        return entity;
    }

    @Override
    public Payment findById(Long id) {
        if (id == null) return null;
        return payments.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(payments);
    }

    @Override
    public void deleteById(Long id) {
        boolean removed = payments.removeIf(p -> p.getId().equals(id));
        if (!removed) throw new RuntimeException("Payment not found");
        saveAll();
    }

    public List<Payment> findByMemberId(Long memberId) {
        return payments.stream().filter(p -> p.getMemberId().equals(memberId)).toList();
    }
}


