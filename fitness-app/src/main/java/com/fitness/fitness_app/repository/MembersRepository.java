package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitness_app.model.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MembersRepository implements BaseRepository<Member> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<Member> members;

    public MembersRepository(@Value("${data.members.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.members = loadFromFile();
    }

    private List<Member> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Member>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading members from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, members);
        } catch (IOException e) {
            throw new RuntimeException("Error saving members to JSON file", e);
        }
    }

    private long getNextId() {
        return members.stream().mapToLong(m -> m.getId() == null ? 0L : m.getId()).max().orElse(0L) + 1;
    }

    @Override
    public Member save(Member entity) {
        if (entity.getId() == null) entity.setId(getNextId());
        for (int i = 0; i < members.size(); i++) {
            if (entity.getId().equals(members.get(i).getId())) {
                members.set(i, entity);
                saveAll();
                return entity;
            }
        }
        members.add(entity);
        saveAll();
        return entity;
    }

    @Override
    public Member findById(Long id) {
        if (id == null) return null;
        return members.stream().filter(m -> id.equals(m.getId())).findFirst().orElse(null);
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members);
    }

    public Member findByEmail(String email) {
        if (email == null) return null;
        return members.stream()
                .filter(m -> m.getEmail() != null && m.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    public Member findByQrCode(String qrCode) {
        if (qrCode == null) return null;
        return members.stream()
                .filter(m -> m.getQrCode() != null && m.getQrCode().equals(qrCode))
                .findFirst().orElse(null);
    }
}

