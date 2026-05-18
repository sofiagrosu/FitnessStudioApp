package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.model.CourseType;
import com.fitness.fitness_app.model.DayOfWeek;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Repository
public class CoursesRepository implements BaseRepository<Course> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<Course> courses;

    public CoursesRepository(@Value("${data.courses.path}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.courses = loadFromFile();
    }

    private List<Course> loadFromFile() {
        File file = JsonFileUtils.resolve(filePath);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Course>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading courses from JSON file", e);
        }
    }

    private void saveAll() {
        try {
            File file = JsonFileUtils.resolve(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, courses);
        } catch (IOException e) {
            throw new RuntimeException("Error saving courses to JSON file", e);
        }
    }

    private Long getNextId() {
        return courses.stream()
                .map(Course::getId)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }

    @Override
    public Course save(Course entity) {
        if (entity == null) throw new RuntimeException("Course data is required");
        if (entity.getId() == null) entity.setId(getNextId());
        for (int i = 0; i < courses.size(); i++) {
            if (entity.getId().equals(courses.get(i).getId())) {
                courses.set(i, entity);
                saveAll();
                return entity;
            }
        }
        courses.add(entity);
        saveAll();
        return entity;
    }

    @Override
    public Course findById(Long id) {
        if (id == null) return null;
        return courses.stream().filter(c -> id.equals(c.getId())).findFirst().orElse(null);
    }

    @Override
    public List<Course> findAll() {
        return new ArrayList<>(courses);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("Course id is required");
        boolean removed = courses.removeIf(c -> id.equals(c.getId()));
        if (!removed) throw new RuntimeException("Course not found");
        saveAll();
    }

    public List<Course> findByLocationId(Long locationId) {
        if (locationId == null) return List.of();
        return courses.stream().filter(c -> locationId.equals(c.getLocationId())).toList();
    }

    public List<Course> findByTrainerId(Long trainerId) {
        if (trainerId == null) return List.of();
        return courses.stream().filter(c -> trainerId.equals(c.getTrainerId())).toList();
    }

    public List<Course> findByType(CourseType type) {
        return courses.stream().filter(c -> c.getType() == type).toList();
    }

    public List<Course> findByDayOfWeek(DayOfWeek dayOfWeek) {
        return courses.stream().filter(c -> c.getDayOfWeek() == dayOfWeek).toList();
    }

    public List<Course> findAvailableCourses() {
        return courses.stream().filter(c -> safeInt(c.getCurrentOccupancy()) < safeInt(c.getMaxCapacity())).toList();
    }

    public List<Course> findFullCourses() {
        return courses.stream().filter(c -> safeInt(c.getCurrentOccupancy()) >= safeInt(c.getMaxCapacity())).toList();
    }

    public List<Course> findByLocationAndType(Long locationId, CourseType type) {
        return courses.stream()
                .filter(c -> locationId != null && locationId.equals(c.getLocationId()) && c.getType() == type)
                .toList();
    }

    public List<Course> searchByName(String keyword) {
        if (keyword == null) return List.of();
        return courses.stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    public List<Course> sortByName() {
        return courses.stream().sorted(Comparator.comparing(Course::getName, Comparator.nullsLast(String::compareToIgnoreCase))).toList();
    }

    public List<Course> sortByStartTime() {
        return courses.stream().sorted(Comparator.comparing(Course::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
    }

    public List<Course> sortByCurrentOccupancyDescending() {
        return courses.stream().sorted(Comparator.comparing((Course c) -> safeInt(c.getCurrentOccupancy())).reversed()).toList();
    }

    public List<Course> sortByMaxCapacityDescending() {
        return courses.stream().sorted(Comparator.comparing((Course c) -> safeInt(c.getMaxCapacity())).reversed()).toList();
    }

    public List<Course> sortByDayAndStartTime() {
        return courses.stream()
                .sorted(Comparator.comparing(Course::getDayOfWeek, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Course::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<Course> sortByLocationAndName() {
        return courses.stream()
                .sorted(Comparator.comparing(Course::getLocationId, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Course::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
