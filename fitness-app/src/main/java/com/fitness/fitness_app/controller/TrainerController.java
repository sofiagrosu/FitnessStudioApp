package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.model.SignUp;
import com.fitness.fitness_app.service.AdminService;
import com.fitness.fitness_app.service.CoursesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint-uri disponibile unui trainer autentificat.
 *
 * Toate rutele sunt sub /trainer/{trainerId} si presupun ca
 * trainerId-ul din path corespunde utilizatorului logat.
 *
 * Operatii disponibile:
 *   GET /trainer/{trainerId}/courses                         - toate cursurile trainerului
 *   GET /trainer/{trainerId}/schedule                        - program sortat dupa zi + ora
 *   GET /trainer/{trainerId}/courses/{courseId}/enrolled     - detalii ocupanta unui curs
 *   GET /trainer/{trainerId}/courses/{courseId}/signups      - lista inscrierilor active
 */
@RestController
@RequestMapping("/trainer")
@CrossOrigin(origins = "http://localhost:3000")
public class TrainerController {

    private final CoursesService coursesService;
    private final AdminService adminService;

    public TrainerController(CoursesService coursesService, AdminService adminService) {
        this.coursesService = coursesService;
        this.adminService = adminService;
    }

    @GetMapping("/{trainerId}/courses")
    public ResponseEntity<List<Course>> getMyCourses(@PathVariable Long trainerId) {
        validateTrainer(trainerId);
        return ResponseEntity.ok(coursesService.getCoursesByTrainer(trainerId));
    }

    @GetMapping("/{trainerId}/schedule")
    public ResponseEntity<List<Course>> getMySchedule(@PathVariable Long trainerId) {
        validateTrainer(trainerId);
        List<Course> courses = coursesService.getCoursesByTrainer(trainerId);
        List<Course> sorted = courses.stream()
                .sorted(java.util.Comparator
                        .comparing(Course::getDayOfWeek)
                        .thenComparing(Course::getStartTime))
                .toList();
        return ResponseEntity.ok(sorted);
    }

    @GetMapping("/{trainerId}/courses/{courseId}/enrolled")
    public ResponseEntity<Map<String, Object>> getEnrolledCount(@PathVariable Long trainerId,
                                                                @PathVariable Long courseId) {
        validateTrainer(trainerId);
        validateCourseOwnedByTrainer(trainerId, courseId);

        Course course = coursesService.getCourseById(courseId);
        int enrolled = coursesService.countEnrolledMembers(courseId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("courseId", courseId);
        result.put("courseName", course.getName());
        result.put("maxCapacity", course.getMaxCapacity());
        result.put("enrolledCount", enrolled);
        result.put("availableSpots", course.getMaxCapacity() - enrolled);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{trainerId}/courses/{courseId}/signups")
    public ResponseEntity<List<SignUp>> getEnrolledSignUps(@PathVariable Long trainerId,
                                                           @PathVariable Long courseId) {
        validateTrainer(trainerId);
        validateCourseOwnedByTrainer(trainerId, courseId);
        return ResponseEntity.ok(coursesService.getEnrolledSignUpsForCourse(courseId));
    }

    private void validateTrainer(Long trainerId) {
        // arunca NotFoundException daca trainer-ul nu exista — GlobalExceptionHandler il transforma in 404
        adminService.getTrainerById(trainerId);
    }

    private void validateCourseOwnedByTrainer(Long trainerId, Long courseId) {
        Course course = coursesService.getCourseById(courseId);
        if (!course.getTrainerId().equals(trainerId)) {
            throw new com.fitness.fitness_app.exception.ForbiddenException(
                    "This course does not belong to the specified trainer");
        }
    }
}
