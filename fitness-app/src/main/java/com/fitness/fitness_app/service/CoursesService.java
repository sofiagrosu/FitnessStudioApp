package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.ForbiddenException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.model.CourseType;
import com.fitness.fitness_app.model.DayOfWeek;
import com.fitness.fitness_app.model.Location;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.model.SignUp;
import com.fitness.fitness_app.model.Trainer;
import com.fitness.fitness_app.model.User;
import com.fitness.fitness_app.model.UserI;
import com.fitness.fitness_app.model.WaitlistEntry;
import com.fitness.fitness_app.repository.CoursesRepository;
import com.fitness.fitness_app.repository.LocationRepository;
import com.fitness.fitness_app.repository.UserRepository;
import com.fitness.fitness_app.repository.SignUpsRepository;
import com.fitness.fitness_app.repository.WaitlistsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class CoursesService {
    private final CoursesRepository coursesRepository;
    private final WaitlistsRepository waitlistsRepository;
    private final SignUpsRepository signUpsRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final MemberService memberService;

    public CoursesService(CoursesRepository coursesRepository,
                          WaitlistsRepository waitlistsRepository,
                          SignUpsRepository signUpsRepository,
                          UserRepository userRepository,
                          LocationRepository locationRepository,
                          MemberService memberService) {
        this.coursesRepository = coursesRepository;
        this.waitlistsRepository = waitlistsRepository;
        this.signUpsRepository = signUpsRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.memberService = memberService;
    }

    public String cancelSignUp(Long signUpId, Long requestingMemberId) {
        if (signUpId == null) throw new ValidationException("Sign-up id is required");
        if (requestingMemberId == null) throw new ValidationException("Member id is required");

        SignUp signUp = signUpsRepository.findById(signUpId);
        if (signUp == null) throw new NotFoundException("Sign-up not found");

        if (!Long.valueOf(signUp.getMemberId()).equals(requestingMemberId)) {
            throw new ForbiddenException("You can only cancel your own sign-ups");
        }

        updateWaitlist(signUp);
        return "Sign-up cancelled successfully";
    }

    public List<String> getAllInformationForFilteredCourses(List<Course> filteredCourses) {
        return filteredCourses.stream().map(Course::toString).toList();
    }

    public List<String> getAllInformationForAllCourses() {
        return coursesRepository.findAll().stream().map(Course::toString).toList();
    }

    private void updateWaitlist(SignUp cancelledSignUp) {
        WaitlistEntry cancelledWaitlistEntry = waitlistsRepository.findBySignUpId(cancelledSignUp.getId());

        // Case 1: inscriere anulata era pe waitlist — se sterge si se recalculeaza pozitiile
        if (cancelledWaitlistEntry != null) {
            waitlistsRepository.deleteById(cancelledWaitlistEntry.getId());
            signUpsRepository.deleteById(cancelledSignUp.getId());
            return;
        }

        // Case 2: inscriere anulata era activa (enrolled) — elibereaza locul
        Course course = coursesRepository.findById(cancelledSignUp.getCourseId());
        if (course == null) throw new NotFoundException("Course not found");

        signUpsRepository.deleteById(cancelledSignUp.getId());
        course.setCurrentOccupancy(Math.max(0, safeInt(course.getCurrentOccupancy()) - 1));

        // Promoveaza primul de pe waitlist (daca exista)
        List<WaitlistEntry> waitlistEntries = waitlistsRepository.findByCourseId(course.getId());
        if (!waitlistEntries.isEmpty()) {
            WaitlistEntry firstWaitlistEntry = waitlistEntries.stream()
                    .min(Comparator.comparing(WaitlistEntry::getPosition))
                    .orElseThrow();
            waitlistsRepository.deleteById(firstWaitlistEntry.getId());
            course.setCurrentOccupancy(safeInt(course.getCurrentOccupancy()) + 1);
        }

        coursesRepository.save(course);
    }

    public String setMemberAttendance(Long signUpId, boolean attended) {
        if (signUpId == null) throw new ValidationException("Sign-up id is required");
        SignUp signUp = signUpsRepository.findById(signUpId);
        if (signUp == null) throw new NotFoundException("Sign-up not found");
        signUp.setAttended(attended);
        signUpsRepository.save(signUp);
        return "Attendance updated successfully";
    }

    public String createSignUp(Long memberId, Long courseId) {
        validateMemberExists(memberId);
        Course course = getCourseById(courseId);

        boolean alreadySignedUp = signUpsRepository.findByCourseIdAndMemberId(courseId, memberId) != null;
        if (alreadySignedUp) throw new ConflictException("Member is already signed up for this course");

        int currentOccupancy = safeInt(course.getCurrentOccupancy());
        int maxCapacity = safeInt(course.getMaxCapacity());

        if (currentOccupancy < maxCapacity) {
            SignUp signUp = new SignUp(null, courseId, memberId, LocalDateTime.now(), false);
            signUpsRepository.save(signUp);
            course.setCurrentOccupancy(currentOccupancy + 1);
            coursesRepository.save(course);
            return "Member successfully signed up for the course";
        }

        if (waitlistsRepository.isCourseWaitlistFull(courseId)) {
            throw new ConflictException("Course and waitlist are both full");
        }

        SignUp signUp = new SignUp(null, courseId, memberId, LocalDateTime.now(), false);
        signUpsRepository.save(signUp);

        WaitlistEntry waitlistEntry = new WaitlistEntry(
                null,
                signUp.getId(),
                memberId,
                courseId,
                waitlistsRepository.countByCourseId(courseId) + 1
        );
        waitlistsRepository.save(waitlistEntry);

        return "Course is full. Member was added to the waitlist at position " + waitlistEntry.getPosition();
    }

    public Course createCourse(Course course) {
        validateCourseData(course);
        validateTrainerExists(course.getTrainerId());
        validateLocationExists(course.getLocationId());

        // Un curs nou incepe intotdeauna cu ocupanta 0; se populeaza prin sign-up-uri.
        course.setCurrentOccupancy(0);
        coursesRepository.save(course);
        return course;
    }

    public String updateCourse(Long courseId, Course updatedCourse) {
        if (courseId == null) throw new ValidationException("Course id is required");

        Course existingCourse = getCourseById(courseId);
        validateCourseData(updatedCourse);
        validateTrainerExists(updatedCourse.getTrainerId());
        validateLocationExists(updatedCourse.getLocationId());

        int existingOccupancy = safeInt(existingCourse.getCurrentOccupancy());
        if (safeInt(updatedCourse.getMaxCapacity()) < existingOccupancy) {
            throw new ValidationException(
                    "Max capacity cannot be lower than current occupancy (" + existingOccupancy + ")");
        }

        updatedCourse.setId(courseId);
        // Ocupanta se pastreaza din cursul existent — nu se accepta din request body.
        updatedCourse.setCurrentOccupancy(existingOccupancy);
        coursesRepository.save(updatedCourse);
        return "Course updated successfully";
    }

    public String deleteCourse(Long courseId) {
        Course course = coursesRepository.findById(courseId);
        if (course == null) throw new NotFoundException("Course not found");

        // Stergem waitlist entries pentru acest curs
        waitlistsRepository.findAll().stream()
                .filter(entry -> Long.valueOf(entry.getCourseId()).equals(courseId))
                .toList()
                .forEach(entry -> waitlistsRepository.deleteById(entry.getId()));

        // Stergem sign-up-urile pentru acest curs
        signUpsRepository.findAll().stream()
                .filter(signUp -> Long.valueOf(signUp.getCourseId()).equals(courseId))
                .toList()
                .forEach(signUp -> signUpsRepository.deleteById(signUp.getId()));

        coursesRepository.deleteById(courseId);
        return "Course deleted successfully";
    }

    public List<Course> getAvailableCourses() {
        return coursesRepository.findAvailableCourses();
    }

    public List<Course> getCoursesByTrainer(Long trainerId) {
        validateTrainerExists(trainerId);
        return coursesRepository.findByTrainerId(trainerId);
    }

    public List<Course> getCoursesByTrainerAndLocation(Long trainerId, Long locationId) {
        validateTrainerExists(trainerId);
        validateLocationExists(locationId);
        return coursesRepository.findByTrainerId(trainerId).stream()
                .filter(course -> locationId.equals(course.getLocationId()))
                .toList();
    }

    public List<Course> getCoursesForMember(Long memberId) {
        validateMemberExists(memberId);
        return signUpsRepository.findByMemberId(memberId).stream()
                .map(s -> coursesRepository.findById(s.getCourseId()))
                .filter(c -> c != null)
                .distinct()
                .toList();
    }

    public List<Course> getPastAttendedCoursesForMember(Long memberId) {
        validateMemberExists(memberId);
        return signUpsRepository.findByMemberId(memberId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getAttended()))
                .map(s -> coursesRepository.findById(s.getCourseId()))
                .filter(c -> c != null)
                .distinct()
                .toList();
    }

    public long countAccumulatedAttendanceForMember(Long memberId) {
        validateMemberExists(memberId);
        return signUpsRepository.findByMemberId(memberId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getAttended()))
                .count();
    }

    public List<Course> getAvailableCoursesByLocation(Long locationId) {
        validateLocationExists(locationId);
        return coursesRepository.findByLocationId(locationId).stream()
                .filter(course -> safeInt(course.getCurrentOccupancy()) < safeInt(course.getMaxCapacity()))
                .toList();
    }

    public List<Course> getCoursesSortedByName() {
        return coursesRepository.sortByName();
    }

    public List<Course> getCoursesSortedByStartTime() {
        return coursesRepository.sortByStartTime();
    }

    public List<Course> getCoursesSortedByCurrentOccupancyDescending() {
        return coursesRepository.sortByCurrentOccupancyDescending();
    }

    public List<Course> getCoursesSortedByMaxCapacityDescending() {
        return coursesRepository.sortByMaxCapacityDescending();
    }

    public List<Course> getCoursesSortedByDayAndStartTime() {
        return coursesRepository.sortByDayAndStartTime();
    }

    public List<Course> getCoursesSortedByLocationAndName() {
        return coursesRepository.sortByLocationAndName();
    }

    public List<Course> getCoursesByLocation(Long locationId) {
        validateLocationExists(locationId);
        return coursesRepository.findByLocationId(locationId);
    }

    public List<Course> getCoursesByType(CourseType type) {
        if (type == null) throw new ValidationException("Course type is required");
        return coursesRepository.findByType(type);
    }

    public List<Course> getCoursesByDayOfWeek(DayOfWeek day) {
        if (day == null) throw new ValidationException("Day of week is required");
        return coursesRepository.findByDayOfWeek(day);
    }

    public List<Course> searchCoursesByName(String keyword) {
        if (keyword == null || keyword.isBlank()) throw new ValidationException("Search keyword is required");
        return coursesRepository.searchByName(keyword);
    }

    public List<Course> getFullCourses() {
        return coursesRepository.findFullCourses();
    }

    public List<Course> getCoursesByLocationAndType(Long locationId, CourseType type) {
        validateLocationExists(locationId);
        if (type == null) throw new ValidationException("Course type is required");
        return coursesRepository.findByLocationAndType(locationId, type);
    }

    public List<WaitlistEntry> getWaitlistForCourse(Long courseId) {
        getCourseById(courseId);
        return waitlistsRepository.findByCourseId(courseId);
    }

    public SignUpStatus getSignUpStatus(Long courseId, Long memberId) {
        getCourseById(courseId);
        validateMemberExists(memberId);

        SignUp signUp = signUpsRepository.findByCourseIdAndMemberId(courseId, memberId);
        if (signUp == null) throw new NotFoundException("No sign-up found for this member and course");

        WaitlistEntry waitlistEntry = waitlistsRepository.findBySignUpId(signUp.getId());
        if (waitlistEntry != null) return new SignUpStatus("WAITLISTED", waitlistEntry.getPosition());
        return new SignUpStatus("ENROLLED", null);
    }

    public record SignUpStatus(String status, Integer waitlistPosition) {}

    public List<Course> getAllCourses() {
        return coursesRepository.findAll();
    }

    public Course getCourseById(Long courseId) {
        if (courseId == null) throw new ValidationException("Course id is required");
        Course course = coursesRepository.findById(courseId);
        if (course == null) throw new NotFoundException("Course not found");
        return course;
    }

    public int countEnrolledMembers(Long courseId) {
        getCourseById(courseId);
        return (int) signUpsRepository.findByCourseId(courseId).stream()
                .filter(s -> waitlistsRepository.findBySignUpId(s.getId()) == null)
                .count();
    }

    public List<SignUp> getEnrolledSignUpsForCourse(Long courseId) {
        getCourseById(courseId);
        return signUpsRepository.findByCourseId(courseId).stream()
                .filter(s -> waitlistsRepository.findBySignUpId(s.getId()) == null)
                .toList();
    }

    private void validateCourseData(Course course) {
        if (course == null) throw new ValidationException("Course data is required");
        if (course.getName() == null || course.getName().isBlank())
            throw new ValidationException("Course name is required");
        if (course.getType() == null) throw new ValidationException("Course type is required");
        if (course.getDayOfWeek() == null) throw new ValidationException("Course day is required");
        if (course.getStartTime() == null) throw new ValidationException("Start time is required");
        if (course.getDuration() == null || course.getDuration() <= 0)
            throw new ValidationException("Duration must be greater than 0");
        if (course.getMaxCapacity() == null || course.getMaxCapacity() <= 0)
            throw new ValidationException("Max capacity must be greater than 0");
        if (course.getTrainerId() == null) throw new ValidationException("Trainer id is required");
        if (course.getLocationId() == null) throw new ValidationException("Location id is required");
    }

  private void validateTrainerExists(Long trainerId) {
    if (trainerId == null) {
        throw new ValidationException("Trainer id is required");
    }

    User user = userRepository.findById(trainerId)
            .orElseThrow(() ->
                    new NotFoundException("Trainer not found"));

    if (!(user instanceof Trainer trainer) || !trainer.isActive()) {
        throw new NotFoundException("Trainer not found or inactive");
    }
}
private void validateLocationExists(Long locationId) {
    if (locationId == null) {
        throw new ValidationException("Location id is required");
    }

    locationRepository.findById(locationId)
            .orElseThrow(() ->
                    new NotFoundException("Location not found"));
}

    private void validateMemberExists(Long memberId) {
        if (memberId == null) throw new ValidationException("Member id is required");
        // getMemberById arunca NotFoundException daca membrul nu exista
        memberService.getMemberById(memberId);
        // Nota: member.isActive() este intotdeauna true (membrii nu pot fi dezactivati)
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
