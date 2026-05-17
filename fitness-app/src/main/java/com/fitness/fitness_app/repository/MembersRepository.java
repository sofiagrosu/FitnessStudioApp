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
public class MembersRepository implements FileRepository<Member> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<Member> members;

    public MembersRepository(@Value("${data.members.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.members = loadFromFile();
    }

    private List<Member> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<List<Member>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading members from JSON file", e);
        }
    }

    private Long getNextId() {
        return members.stream().map(Member::getId).max(Long::compareTo).orElse(0L) + 1;
    }

    @Override
    public List<Member> getAll() { return new ArrayList<>(members); }

    @Override
    public void SaveAll(List<Member> items) {
        this.members = new ArrayList<>(items);
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, this.members);
        } catch (IOException e) {
            throw new RuntimeException("Error saving members to JSON file", e);
        }
    }

    @Override
    public void add(Member item) {
        if (item.getId() == null) item.setId(getNextId());
        members.add(item);
        SaveAll(members);
    }

    @Override
    public void update(Member item) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(item.getId())) {
                members.set(i, item);
                SaveAll(members);
                return;
            }
        }
        throw new RuntimeException("Member not found");
    }

    @Override
    public void delete(Long id) {
        Member member = findById(id);
        if (member == null) throw new RuntimeException("Member not found");
        member.setActive(false);
        update(member);
    }

    @Override
    public Member findById(Long id) {
        return members.stream().filter(member -> member.getId().equals(id)).findFirst().orElse(null);
    }

    public Member findByEmail(String email) {
        return members.stream()
                .filter(member -> member.getEmail() != null && member.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    public Member findByQrCode(String qrCode) {
        return members.stream()
                .filter(member -> member.getQrCode() != null && member.getQrCode().equals(qrCode))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> getAllInformation() {
        return members.stream().map(Member::toString).toList();
    }
}
