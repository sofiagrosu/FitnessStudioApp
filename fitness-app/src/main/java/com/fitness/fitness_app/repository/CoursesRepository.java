package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.model.CourseType;
import com.fitness.fitness_app.model.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursesRepository extends JpaRepository<Course, Long> {

    List<Course> findByLocation_Id(Long locationId);

    List<Course> findByTrainer_Id(Long trainerId);

    List<Course> findByType(CourseType type);

    List<Course> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<Course> findByLocation_IdAndType(Long locationId,
                                          CourseType type);

    List<Course> findByNameContainingIgnoreCase(String keyword);

    List<Course> findByCurrentOccupancyLessThan(Integer maxCapacity);

    List<Course> findByCurrentOccupancyGreaterThanEqual(Integer maxCapacity);
}