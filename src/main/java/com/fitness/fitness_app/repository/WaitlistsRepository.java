package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitness_app.domain.WaitlistEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WaitlistsRepository implements FileRepository<WaitlistEntry> {

    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<WaitlistEntry> entries;

    private static final int MAX_WAITLIST_SIZE_PER_COURSE = 5;

    public WaitlistsRepository(String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.entries = loadFromFile();
    }

    private List<WaitlistEntry> loadFromFile() {
        File file = new File(filePath);

        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    file,
                    new TypeReference<List<WaitlistEntry>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error reading waitlist entries from JSON file", e
            );
        }
    }

    @Override
    public List<WaitlistEntry> getAll() {
        return new ArrayList<>(entries);
    }

    @Override
    public void SaveAll(List<WaitlistEntry> items) {
        this.entries = new ArrayList<>(items);

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(filePath), this.entries);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error saving waitlist entries to JSON file", e
            );
        }
    }

    @Override
    public void add(WaitlistEntry item) {
        long entriesForCourse = entries.stream()
                .filter(entry -> entry.getCourseId().equals(item.getCourseId()))
                .count();

        if (entriesForCourse >= MAX_WAITLIST_SIZE_PER_COURSE) {
            throw new RuntimeException(
                    "Waitlist is full for this course. Maximum size is 5."
            );
        }

        int nextPosition = entries.stream()
                .filter(entry -> entry.getCourseId().equals(item.getCourseId()))
                .map(WaitlistEntry::getPosition)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        item.setPosition(nextPosition);
        entries.add(item);

        SaveAll(entries);
    }

    @Override
    public void update(WaitlistEntry item) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(item.getId())) {
                entries.set(i, item);
                SaveAll(entries);
                return;
            }
        }

        throw new RuntimeException("Waitlist entry not found");
    }

    @Override
    public void delete(Long id) {
        WaitlistEntry entryToDelete = entries.stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Waitlist entry not found")
                );

        Long courseId = entryToDelete.getCourseId();

        entries.removeIf(entry -> entry.getId().equals(id));

        reorderPositionsForCourse(courseId);

        SaveAll(entries);
    }

    public List<WaitlistEntry> findByCourseId(Long courseId) {
        return entries.stream()
                .filter(entry -> entry.getCourseId().equals(courseId))
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .toList();
    }

    public WaitlistEntry findBySignUpId(Long signUpId) {
        return entries.stream()
                .filter(entry -> entry.getSignUpId().equals(signUpId))
                .findFirst()
                .orElse(null);
    }

    public boolean isCourseWaitlistFull(Long courseId) {
        return entries.stream()
                .filter(entry -> entry.getCourseId().equals(courseId))
                .count() >= MAX_WAITLIST_SIZE_PER_COURSE;
    }

    public int countByCourseId(Long courseId) {
        return (int) entries.stream()
                .filter(entry -> entry.getCourseId().equals(courseId))
                .count();
    }

    public List<WaitlistEntry> sortByCourseIdAndPosition() {
        return entries.stream()
                .sorted(
                        Comparator.comparing(WaitlistEntry::getCourseId)
                                .thenComparing(WaitlistEntry::getPosition)
                )
                .toList();
    }

    public List<WaitlistEntry> sortByPosition() {
        return entries.stream()
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .toList();
    }

    private void reorderPositionsForCourse(Long courseId) {
        List<WaitlistEntry> courseEntries = entries.stream()
                .filter(entry -> entry.getCourseId().equals(courseId))
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .toList();

        for (int i = 0; i < courseEntries.size(); i++) {
            courseEntries.get(i).setPosition(i + 1);
        }
    }
}