package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitness_app.model.WaitlistEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
public class WaitlistsRepository implements BaseRepository<WaitlistEntry> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<WaitlistEntry> entries;

    private static final int MAX_WAITLIST_SIZE_PER_COURSE = 5;

    public WaitlistsRepository(@Value("${data.waitlist.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.entries = loadFromFile();
    }

    private List<WaitlistEntry> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<WaitlistEntry>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading waitlist entries from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, entries);
        } catch (IOException e) {
            throw new RuntimeException("Error saving waitlist entries to JSON file", e);
        }
    }

    private Long getNextId() {
        return entries.stream().map(WaitlistEntry::getId).mapToLong(Long::longValue).max().orElse(0L) + 1;
    }

    @Override
    public WaitlistEntry save(WaitlistEntry entity) {
        if (entity.getId() == null) {
            // new entry â€” apply full add logic (size check, position assignment)
            entity.setId(getNextId());
            long entriesForCourse = entries.stream()
                    .filter(e -> e.getCourseId().equals(entity.getCourseId()))
                    .count();
            if (entriesForCourse >= MAX_WAITLIST_SIZE_PER_COURSE)
                throw new RuntimeException("Waitlist is full for this course. Maximum size is " + MAX_WAITLIST_SIZE_PER_COURSE + ".");
            int nextPosition = entries.stream()
                    .filter(e -> e.getCourseId().equals(entity.getCourseId()))
                    .map(WaitlistEntry::getPosition)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
            entity.setPosition(nextPosition);
            entries.add(entity);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getId().equals(entity.getId())) {
                    entries.set(i, entity);
                    saveAll();
                    return entity;
                }
            }
            entries.add(entity);
        }
        saveAll();
        return entity;
    }

    @Override
    public WaitlistEntry findById(Long id) {
        if (id == null) return null;
        return entries.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<WaitlistEntry> findAll() {
        return new ArrayList<>(entries);
    }

    @Override
    public void deleteById(Long id) {
        WaitlistEntry toDelete = entries.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));
        Long courseId = toDelete.getCourseId();
        entries.removeIf(e -> e.getId().equals(id));
        reorderPositionsForCourse(courseId);
        saveAll();
    }

    public List<WaitlistEntry> findByCourseId(Long courseId) {
        return entries.stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .toList();
    }

    public WaitlistEntry findBySignUpId(Long signUpId) {
        return entries.stream().filter(e -> e.getSignUpId().equals(signUpId)).findFirst().orElse(null);
    }

    public List<WaitlistEntry> findByMemberId(Long memberId) {
        return entries.stream()
                .filter(e -> e.getMemberId() != null && e.getMemberId().equals(memberId))
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .toList();
    }

    public boolean isCourseWaitlistFull(Long courseId) {
        return entries.stream().filter(e -> e.getCourseId().equals(courseId)).count() >= MAX_WAITLIST_SIZE_PER_COURSE;
    }

    public int countByCourseId(Long courseId) {
        return (int) entries.stream().filter(e -> e.getCourseId().equals(courseId)).count();
    }

    public List<WaitlistEntry> sortByCourseIdAndPosition() {
        return entries.stream()
                .sorted(Comparator.comparing(WaitlistEntry::getCourseId).thenComparing(WaitlistEntry::getPosition))
                .toList();
    }

    public List<WaitlistEntry> sortByPosition() {
        return entries.stream().sorted(Comparator.comparing(WaitlistEntry::getPosition)).toList();
    }

    private void reorderPositionsForCourse(Long courseId) {
        List<WaitlistEntry> courseEntries = entries.stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .toList();
        for (int i = 0; i < courseEntries.size(); i++) {
            courseEntries.get(i).setPosition(i + 1);
        }
    }
}


