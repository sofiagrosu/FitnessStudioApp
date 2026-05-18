package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.fitness_app.model.Receipt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReceiptsRepository implements BaseRepository<Receipt> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<Receipt> receipts;

    public ReceiptsRepository(@Value("${data.receipts.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.receipts = loadFromFile();
    }

    private List<Receipt> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Receipt>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading receipts from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, receipts);
        } catch (IOException e) {
            throw new RuntimeException("Error saving receipts to JSON file", e);
        }
    }

    private Long getNextId() {
        return receipts.stream().map(Receipt::getId).mapToLong(Long::longValue).max().orElse(0L) + 1;
    }

    @Override
    public Receipt save(Receipt entity) {
        if (entity.getId() == null) entity.setId(getNextId());
        for (int i = 0; i < receipts.size(); i++) {
            if (receipts.get(i).getId().equals(entity.getId())) {
                receipts.set(i, entity);
                saveAll();
                return entity;
            }
        }
        receipts.add(entity);
        saveAll();
        return entity;
    }

    @Override
    public Receipt findById(Long id) {
        if (id == null) return null;
        return receipts.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Receipt> findAll() {
        return new ArrayList<>(receipts);
    }

    @Override
    public void deleteById(Long id) {
        boolean removed = receipts.removeIf(r -> r.getId().equals(id));
        if (!removed) throw new RuntimeException("Receipt not found");
        saveAll();
    }

    public Receipt findByPaymentId(Long paymentId) {
        return receipts.stream().filter(r -> r.getPaymentId().equals(paymentId)).findFirst().orElse(null);
    }
}


