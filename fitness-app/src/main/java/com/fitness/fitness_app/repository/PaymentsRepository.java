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
public class PaymentsRepository implements FileRepository<Payment> {
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
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Payment>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading payments from JSON file", e);
        }
    }

    private Long getNextId() { return payments.stream().map(Payment::getId).max(Long::compareTo).orElse(0L) + 1; }

    @Override public List<Payment> getAll() { return new ArrayList<>(payments); }

    @Override
    public void SaveAll(List<Payment> items) {
        this.payments = new ArrayList<>(items);
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, this.payments);
        } catch (IOException e) {
            throw new RuntimeException("Error saving payments to JSON file", e);
        }
    }

    @Override public void add(Payment item) { if (item.getId() == null) item.setId(getNextId()); payments.add(item); SaveAll(payments); }

    @Override
    public void update(Payment item) {
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getId().equals(item.getId())) { payments.set(i, item); SaveAll(payments); return; }
        }
        throw new RuntimeException("Payment not found");
    }

    @Override public void delete(Long id) { boolean removed = payments.removeIf(payment -> payment.getId().equals(id)); if (!removed) throw new RuntimeException("Payment not found"); SaveAll(payments); }
    @Override public Payment findById(Long id) { return payments.stream().filter(payment -> payment.getId().equals(id)).findFirst().orElse(null); }
    public List<Payment> findByMemberId(Long memberId) { return payments.stream().filter(payment -> payment.getMemberId().equals(memberId)).toList(); }
    @Override public List<String> getAllInformation() { return payments.stream().map(Payment::toString).toList(); }
}
