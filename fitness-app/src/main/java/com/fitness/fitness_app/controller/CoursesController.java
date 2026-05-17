package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.service.CoursesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "http://localhost:3000")
public class CoursesController {

    private final CoursesService coursesService;

    public CoursesController(CoursesService coursesService) {
        this.coursesService = coursesService;
    }

    // Get all courses
    @GetMapping
    public ResponseEntity<List<String>> getAllCourses() {
        return ResponseEntity.ok(
                coursesService.getAllInformationForAllCourses()
        );
    }

    // Create course
    @PostMapping
    public ResponseEntity<String> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(
                coursesService.createCourse(course)
        );
    }

    // Update course
    @PutMapping("/{courseId}")
    public ResponseEntity<String> updateCourse(
            @PathVariable Long courseId,
            @RequestBody Course course
    ) {
        return ResponseEntity.ok(
                coursesService.updateCourse(courseId, course)
        );
    }

    // Delete course
    @DeleteMapping("/{courseId}")
    public ResponseEntity<String> deleteCourse(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                coursesService.deleteCourse(courseId)
        );
    }

    // Sign up member to course
    @PostMapping("/{courseId}/signups")
    public ResponseEntity<String> createSignUp(
            @PathVariable Long courseId,
            @RequestBody SignUpRequest request
    ) {
        return ResponseEntity.ok(
                coursesService.createSignUp(
                        request.memberId(),
                        courseId
                )
        );
    }

    // Cancel sign-up
    @DeleteMapping("/signups/{signUpId}")
    public ResponseEntity<String> cancelSignUp(
            @PathVariable Long signUpId
    ) {
        return ResponseEntity.ok(
                coursesService.cancelSignUp(signUpId)
        );
    }

    // Set attendance
    @PatchMapping("/signups/{signUpId}/attendance")
    public ResponseEntity<String> setAttendance(
            @PathVariable Long signUpId,
            @RequestBody AttendanceRequest request
    ) {
        return ResponseEntity.ok(
                coursesService.setMemberAttendance(
                        signUpId,
                        request.attended()
                )
        );
    }

    // Filter by available courses
    @GetMapping("/available")
    public ResponseEntity<List<Course>> getAvailableCourses() {
        return ResponseEntity.ok(
                coursesService.getAvailableCourses()
        );
    }

    // Filter by location
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Course>> getCoursesByLocation(
            @PathVariable Long locationId
    ) {
        return ResponseEntity.ok(
                coursesService.getCoursesByLocation(locationId)
        );
    }

    // Filter available by location
    @GetMapping("/location/{locationId}/available")
    public ResponseEntity<List<Course>> getAvailableCoursesByLocation(
            @PathVariable Long locationId
    ) {
        return ResponseEntity.ok(
                coursesService.getAvailableCoursesByLocation(locationId)
        );
    }

    // Filter by trainer
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<Course>> getCoursesByTrainer(
            @PathVariable Long trainerId
    ) {
        return ResponseEntity.ok(
                coursesService.getCoursesByTrainer(trainerId)
        );
    }

    // Filter by trainer and location
    @GetMapping("/trainer/{trainerId}/location/{locationId}")
    public ResponseEntity<List<Course>> getCoursesByTrainerAndLocation(
            @PathVariable Long trainerId,
            @PathVariable Long locationId
    ) {
        return ResponseEntity.ok(
                coursesService.getCoursesByTrainerAndLocation(
                        trainerId,
                        locationId
                )
        );
    }

    // Courses for member
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Course>> getCoursesForMember(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                coursesService.getCoursesForMember(memberId)
        );
    }

    // Past attended courses for member
    @GetMapping("/member/{memberId}/attended")
    public ResponseEntity<List<Course>> getPastAttendedCoursesForMember(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                coursesService.getPastAttendedCoursesForMember(memberId)
        );
    }

    // Sorting
    @GetMapping("/sort/name")
    public ResponseEntity<List<Course>> sortByName() {
        return ResponseEntity.ok(
                coursesService.getCoursesSortedByName()
        );
    }

    @GetMapping("/sort/start-time")
    public ResponseEntity<List<Course>> sortByStartTime() {
        return ResponseEntity.ok(
                coursesService.getCoursesSortedByStartTime()
        );
    }

    @GetMapping("/sort/current-occupancy")
    public ResponseEntity<List<Course>> sortByCurrentOccupancy() {
        return ResponseEntity.ok(
                coursesService.getCoursesSortedByCurrentOccupancyDescending()
        );
    }

    @GetMapping("/sort/max-capacity")
    public ResponseEntity<List<Course>> sortByMaxCapacity() {
        return ResponseEntity.ok(
                coursesService.getCoursesSortedByMaxCapacityDescending()
        );
    }

    @GetMapping("/sort/day-start-time")
    public ResponseEntity<List<Course>> sortByDayAndStartTime() {
        return ResponseEntity.ok(
                coursesService.getCoursesSortedByDayAndStartTime()
        );
    }

    @GetMapping("/sort/location-name")
    public ResponseEntity<List<Course>> sortByLocationAndName() {
        return ResponseEntity.ok(
                coursesService.getCoursesSortedByLocationAndName()
        );
    }

    // Simple error handler
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    public record SignUpRequest(Long memberId) {}

    public record AttendanceRequest(Boolean attended) {}
}