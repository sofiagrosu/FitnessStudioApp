package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.model.CourseType;
import com.fitness.fitness_app.model.DayOfWeek;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;


@Repository
public class CoursesRepository implements FileRepository<Course> {

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
        File file = new File(filePath);

        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    file,
                    new TypeReference<List<Course>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error reading courses from JSON file", e
            );
        }
    }

    
    @Override
    public List<Course> getAll() {
        return new ArrayList<>(courses);
    }

    @Override
    public void SaveAll(List<Course> items) {
        this.courses = new ArrayList<>(items);

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(filePath), this.courses);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error saving courses to JSON file", e
            );
        }
    }

    @Override
    public void add(Course item) {
        item.setId(getNextId());
        courses.add(item);
        SaveAll(courses);
    }

    public Long getNextId() {
        return courses.stream()
                .map(Course::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }

    @Override
    public void update(Course item) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId().equals(item.getId())) {
                courses.set(i, item);
                SaveAll(courses);
                return;
            }
        }

        throw new RuntimeException("Course not found");
    }

    @Override
    public void delete(Long id) {
        boolean removed = courses.removeIf(
                course -> course.getId().equals(id)
        );

        if (!removed) {
            throw new RuntimeException("Course not found");
        }

        SaveAll(courses);
    }
    

    // Filters

    public Course findById(Long id) {
        return courses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Course> findByLocationId(Long locationId) {
        return courses.stream()
                .filter(course -> course.getLocationId().equals(locationId))
                .toList();
    }

    public List<Course> findByTrainerId(Long trainerId) {
        return courses.stream()
                .filter(course -> course.getTrainerId().equals(trainerId))
                .toList();
    }

    public List<Course> findByType(CourseType type) {
        return courses.stream()
                .filter(course -> course.getType() == type)
                .toList();
    }

    public List<Course> findByDayOfWeek(DayOfWeek dayOfWeek) {
        return courses.stream()
                .filter(course -> course.getDayOfWeek() == dayOfWeek)
                .toList();
    }

    public List<Course> findAvailableCourses() {
        return courses.stream()
                .filter(course ->
                        course.getCurrentOccupancy() < course.getMaxCapacity()
                )
                .toList();
    }

    public List<Course> findFullCourses() {
        return courses.stream()
                .filter(course ->
                        course.getCurrentOccupancy().equals(course.getMaxCapacity())
                )
                .toList();
    }

    public List<Course> findByLocationAndType(Long locationId, CourseType type) {
        return courses.stream()
                .filter(course -> course.getLocationId().equals(locationId))
                .filter(course -> course.getType() == type)
                .toList();
    }

    public List<Course> searchByName(String keyword) {
        return courses.stream()
                .filter(course ->
                        course.getName()
                                .toLowerCase()
                                .contains(keyword.toLowerCase())
                )
                .toList();
    }

    // Sorting

    public List<Course> sortByName() {
        return courses.stream()
                .sorted(Comparator.comparing(Course::getName))
                .toList();
    }

    public List<Course> sortByStartTime() {
        return courses.stream()
                .sorted(Comparator.comparing(Course::getStartTime))
                .toList();
    }

    public List<Course> sortByCurrentOccupancyDescending() {
        return courses.stream()
                .sorted(
                        Comparator.comparing(Course::getCurrentOccupancy)
                                .reversed()
                )
                .toList();
    }

    public List<Course> sortByMaxCapacityDescending() {
        return courses.stream()
                .sorted(
                        Comparator.comparing(Course::getMaxCapacity)
                                .reversed()
                )
                .toList();
    }

    public List<Course> sortByDayAndStartTime() {
        return courses.stream()
                .sorted(
                        Comparator.comparing(Course::getDayOfWeek)
                                .thenComparing(Course::getStartTime)
                )
                .toList();
    }

    public List<Course> sortByLocationAndName() {
        return courses.stream()
                .sorted(
                        Comparator.comparing(Course::getLocationId)
                                .thenComparing(Course::getName)
                )
                .toList();
    }

   
    //get all information override interface method
    public List<String> getAllInformation() {
        return courses.stream()
                .map(Course::toString)
                .toList();
    }
}