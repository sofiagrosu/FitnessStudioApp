package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.fitness_app.model.Subscription;
import com.fitness.fitness_app.model.enums.SubscriptionStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Repository
public class SubscriptionsRepository implements BaseRepository<Subscription> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<Subscription> subscriptions;

    public SubscriptionsRepository(@Value("${data.subscriptions.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.subscriptions = loadFromFile();
    }

    private List<Subscription> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Subscription>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading subscriptions from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, subscriptions);
        } catch (IOException e) {
            throw new RuntimeException("Error saving subscriptions to JSON file", e);
        }
    }

    private Long getNextId() {
        return subscriptions.stream()
                .map(Subscription::getId)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }

    @Override
    public Subscription save(Subscription entity) {
        if (entity.getId() == null) entity.setId(getNextId());
        for (int i = 0; i < subscriptions.size(); i++) {
            if (subscriptions.get(i).getId().equals(entity.getId())) {
                subscriptions.set(i, entity);
                saveAll();
                return entity;
            }
        }
        subscriptions.add(entity);
        saveAll();
        return entity;
    }

    @Override
    public Subscription findById(Long id) {
        if (id == null) return null;
        return subscriptions.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Subscription> findAll() {
        return new ArrayList<>(subscriptions);
    }

    @Override
    public void deleteById(Long id) {
        boolean removed = subscriptions.removeIf(s -> s.getId().equals(id));
        if (!removed) throw new RuntimeException("Subscription not found");
        saveAll();
    }

    public Subscription findActiveByMemberId(Long memberId) {
        if (memberId == null) return null;
        return subscriptions.stream()
                .filter(s -> memberId.equals(s.getMemberId()))
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .max(Comparator.comparing(Subscription::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    public List<Subscription> findByMemberId(Long memberId) {
        if (memberId == null) return List.of();
        return subscriptions.stream().filter(s -> memberId.equals(s.getMemberId())).toList();
    }
}


