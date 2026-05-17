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

@Repository
public class SubscriptionsRepository implements FileRepository<Subscription> {
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
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Subscription>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading subscriptions from JSON file", e);
        }
    }

    private Long getNextId() {
        return subscriptions.stream().map(Subscription::getId).max(Long::compareTo).orElse(0L) + 1;
    }

    @Override public List<Subscription> getAll() { return new ArrayList<>(subscriptions); }

    @Override
    public void SaveAll(List<Subscription> items) {
        this.subscriptions = new ArrayList<>(items);
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, this.subscriptions);
        } catch (IOException e) {
            throw new RuntimeException("Error saving subscriptions to JSON file", e);
        }
    }

    @Override
    public void add(Subscription item) {
        if (item.getId() == null) item.setId(getNextId());
        subscriptions.add(item);
        SaveAll(subscriptions);
    }

    @Override
    public void update(Subscription item) {
        for (int i = 0; i < subscriptions.size(); i++) {
            if (subscriptions.get(i).getId().equals(item.getId())) {
                subscriptions.set(i, item);
                SaveAll(subscriptions);
                return;
            }
        }
        throw new RuntimeException("Subscription not found");
    }

    @Override
    public void delete(Long id) {
        boolean removed = subscriptions.removeIf(subscription -> subscription.getId().equals(id));
        if (!removed) throw new RuntimeException("Subscription not found");
        SaveAll(subscriptions);
    }

    @Override
    public Subscription findById(Long id) {
        return subscriptions.stream().filter(subscription -> subscription.getId().equals(id)).findFirst().orElse(null);
    }

    public Subscription findActiveByMemberId(Long memberId) {
        return subscriptions.stream()
                .filter(subscription -> subscription.getMemberId().equals(memberId))
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE)
                .max(Comparator.comparing(Subscription::getStartDate))
                .orElse(null);
    }

    public List<Subscription> findByMemberId(Long memberId) {
        return subscriptions.stream().filter(subscription -> subscription.getMemberId().equals(memberId)).toList();
    }

    @Override
    public List<String> getAllInformation() { return subscriptions.stream().map(Subscription::toString).toList(); }
}
