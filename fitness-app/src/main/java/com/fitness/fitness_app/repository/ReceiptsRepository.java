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
public class ReceiptsRepository implements FileRepository<Receipt> {
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
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try { return objectMapper.readValue(file, new TypeReference<List<Receipt>>() {}); }
        catch (IOException e) { throw new RuntimeException("Error reading receipts from JSON file", e); }
    }

    private Long getNextId() { return receipts.stream().map(Receipt::getId).max(Long::compareTo).orElse(0L) + 1; }
    @Override public List<Receipt> getAll() { return new ArrayList<>(receipts); }

    @Override
    public void SaveAll(List<Receipt> items) {
        this.receipts = new ArrayList<>(items);
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, this.receipts);
        } catch (IOException e) { throw new RuntimeException("Error saving receipts to JSON file", e); }
    }

    @Override public void add(Receipt item) { if (item.getId() == null) item.setId(getNextId()); receipts.add(item); SaveAll(receipts); }
    @Override public void update(Receipt item) { for (int i=0;i<receipts.size();i++) { if (receipts.get(i).getId().equals(item.getId())) { receipts.set(i,item); SaveAll(receipts); return; } } throw new RuntimeException("Receipt not found"); }
    @Override public void delete(Long id) { boolean removed=receipts.removeIf(receipt -> receipt.getId().equals(id)); if(!removed) throw new RuntimeException("Receipt not found"); SaveAll(receipts); }
    @Override public Receipt findById(Long id) { return receipts.stream().filter(receipt -> receipt.getId().equals(id)).findFirst().orElse(null); }
    public Receipt findByPaymentId(Long paymentId) { return receipts.stream().filter(receipt -> receipt.getPaymentId().equals(paymentId)).findFirst().orElse(null); }
    @Override public List<String> getAllInformation() { return receipts.stream().map(Receipt::toString).toList(); }
}
