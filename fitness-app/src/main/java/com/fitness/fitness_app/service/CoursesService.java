package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.ForbiddenException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.*;
import com.fitness.fitness_app.repository.*;
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
    private final SubscriptionService subscriptionService;

    public CoursesService(CoursesRepository coursesRepository,
                          WaitlistsRepository waitlistsRepository,
                          SignUpsRepository signUpsRepository,
                          UserRepository userRepository,
                          LocationRepository locationRepository,
                          MemberService memberService,
                          SubscriptionService subscriptionService) {
        this.coursesRepository = coursesRepository;
        this.waitlistsRepository = waitlistsRepository;
        this.signUpsRepository = signUpsRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.memberService = memberService;
        this.subscriptionService = subscriptionService;
    }

    public String cancelSignUp(Long signUpId, Long requestingMemberId) {
        if (signUpId == null) throw new ValidationException("Sign-up id is required");
        if (requestingMemberId == null) throw new ValidationException("Member id is required");

        SignUp signUp = signUpsRepository.findById(signUpId)
                .orElseThrow(() -> new NotFoundException("Sign-up not found"));

        if (!signUp.getMemberId().equals(requestingMemberId)) {
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
        WaitlistEntry cancelledWaitlistEntry = waitlistsRepository.findBySignUp_Id(cancelledSignUp.getId());

        if (cancelledWaitlistEntry != null) {
            waitlistsRepository.deleteById(cancelledWaitlistEntry.getId());
            signUpsRepository.deleteById(cancelledSignUp.getId());
            return;
        }

        Course course = getCourseById(cancelledSignUp.getCourseId());

        signUpsRepository.deleteById(cancelledSignUp.getId());
        course.setCurrentOccupancy(Math.max(0, safeInt(course.getCurrentOccupancy()) - 1));

        List<WaitlistEntry> waitlistEntries = waitlistsRepository.findByCourse_IdOrderByPositionAsc(course.getId());

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

        SignUp signUp = signUpsRepository.findById(signUpId)
                .orElseThrow(() -> new NotFoundException("Sign-up not found"));

        signUp.setAttended(attended);
        signUpsRepository.save(signUp);

        return "Attendance updated successfully";
    }

    public String createSignUp(Long memberId, Long courseId) {
        Member member = memberService.getMemberById(memberId);
        Course course = getCourseById(courseId);

        Subscription subscription = subscriptionService.getActiveSubscriptionForMember(memberId);
        if (subscription == null || !subscription.isPaid()) {
            throw new ValidationException("Member must have an active paid subscription to join a course");
        }

        boolean alreadySignedUp = signUpsRepository.findByCourse_IdAndMember_Id(courseId, memberId) != null;
        if (alreadySignedUp) throw new ConflictException("Member is already signed up for this course");

        int currentOccupancy = safeInt(course.getCurrentOccupancy());
        int maxCapacity = safeInt(course.getMaxCapacity());

        if (currentOccupancy < maxCapacity) {
            SignUp signUp = new SignUp(course, member, LocalDateTime.now(), false);
            signUpsRepository.save(signUp);

            course.setCurrentOccupancy(currentOccupancy + 1);
            coursesRepository.save(course);

            return "Member successfully signed up for the course";
        }

        if (waitlistsRepository.isCourseWaitlistFull(courseId)) {
            throw new ConflictException("Course and waitlist are both full");
        }

        SignUp signUp = new SignUp(course, member, LocalDateTime.now(), false);
        signUpsRepository.save(signUp);

        WaitlistEntry waitlistEntry = new WaitlistEntry(
                signUp,
                member,
                course,
                waitlistsRepository.countByCourse_Id(courseId) + 1
        );

        waitlistsRepository.save(waitlistEntry);

        return "Course is full. Member was added to the waitlist at position " + waitlistEntry.getPosition();
    }

    public Course createCourse(Course course) {
        validateCourseData(course);

        Trainer trainer = getTrainerById(course.getTrainer().getId());
        Location location = getLocationById(course.getLocation().getId());

        course.setTrainer(trainer);
        course.setLocation(location);
        course.setCurrentOccupancy(0);

        return coursesRepository.save(course);
    }

    public String updateCourse(Long courseId, Course updatedCourse) {
        Course existingCourse = getCourseById(courseId);
        validateCourseData(updatedCourse);

        Trainer trainer = getTrainerById(updatedCourse.getTrainer().getId());
        Location location = getLocationById(updatedCourse.getLocation().getId());

        int existingOccupancy = safeInt(existingCourse.getCurrentOccupancy());

        if (safeInt(updatedCourse.getMaxCapacity()) < existingOccupancy) {
            throw new ValidationException("Max capacity cannot be lower than current occupancy (" + existingOccupancy + ")");
        }

        existingCourse.setTrainer(trainer);
        existingCourse.setLocation(location);
        existingCourse.setName(updatedCourse.getName());
        existingCourse.setType(updatedCourse.getType());
        existingCourse.setDayOfWeek(updatedCourse.getDayOfWeek());
        existingCourse.setStartTime(updatedCourse.getStartTime());
        existingCourse.setDuration(updatedCourse.getDuration());
        existingCourse.setMaxCapacity(updatedCourse.getMaxCapacity());
        existingCourse.setRecurring(updatedCourse.getRecurring());

        coursesRepository.save(existingCourse);
        return "Course updated successfully";
    }

    public String deleteCourse(Long courseId) {
        Course course = getCourseById(courseId);

        waitlistsRepository.findByCourse_IdOrderByPositionAsc(courseId)
                .forEach(entry -> waitlistsRepository.deleteById(entry.getId()));

        signUpsRepository.findByCourse_Id(courseId)
                .forEach(signUp -> signUpsRepository.deleteById(signUp.getId()));

        coursesRepository.delete(course);
        return "Course deleted successfully";
    }

    public List<Course> getAvailableCourses() {
        return coursesRepository.findAll().stream()
                .filter(course -> safeInt(course.getCurrentOccupancy()) < safeInt(course.getMaxCapacity()))
                .toList();
    }

    public List<Course> getCoursesByTrainer(Long trainerId) {
        validateTrainerExists(trainerId);
        return coursesRepository.findByTrainer_Id(trainerId);
    }

    public List<Course> getCoursesByTrainerAndLocation(Long trainerId, Long locationId) {
        validateTrainerExists(trainerId);
        validateLocationExists(locationId);

        return coursesRepository.findByTrainer_Id(trainerId).stream()
                .filter(course -> course.getLocation() != null && locationId.equals(course.getLocation().getId()))
                .toList();
    }

    public List<Course> getCoursesForMember(Long memberId) {
        validateMemberExists(memberId);

        return signUpsRepository.findByMember_Id(memberId).stream()
                .map(SignUp::getCourse)
                .distinct()
                .toList();
    }

    public List<Course> getPastAttendedCoursesForMember(Long memberId) {
        validateMemberExists(memberId);

        return signUpsRepository.findByMember_Id(memberId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getAttended()))
                .map(SignUp::getCourse)
                .distinct()
                .toList();
    }

    public long countAccumulatedAttendanceForMember(Long memberId) {
        validateMemberExists(memberId);

        return signUpsRepository.findByMember_Id(memberId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getAttended()))
                .count();
    }

    public List<Course> getAvailableCoursesByLocation(Long locationId) {
        validateLocationExists(locationId);

        return coursesRepository.findByLocation_Id(locationId).stream()
                .filter(course -> safeInt(course.getCurrentOccupancy()) < safeInt(course.getMaxCapacity()))
                .toList();
    }

    public List<Course> getCoursesSortedByName() {
        return coursesRepository.findAll().stream()
                .sorted(Comparator.comparing(Course::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public List<Course> getCoursesSortedByStartTime() {
        return coursesRepository.findAll().stream()
                .sorted(Comparator.comparing(Course::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<Course> getCoursesSortedByCurrentOccupancyDescending() {
        return coursesRepository.findAll().stream()
                .sorted(Comparator.comparing((Course c) -> safeInt(c.getCurrentOccupancy())).reversed())
                .toList();
    }

    public List<Course> getCoursesSortedByMaxCapacityDescending() {
        return coursesRepository.findAll().stream()
                .sorted(Comparator.comparing((Course c) -> safeInt(c.getMaxCapacity())).reversed())
                .toList();
    }

    public List<Course> getCoursesSortedByDayAndStartTime() {
        return coursesRepository.findAll().stream()
                .sorted(Comparator.comparing(Course::getDayOfWeek, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Course::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<Course> getCoursesSortedByLocationAndName() {
        return coursesRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Course c) -> c.getLocation() == null ? null : c.getLocation().getId(),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Course::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public List<Course> getCoursesByLocation(Long locationId) {
        validateLocationExists(locationId);
        return coursesRepository.findByLocation_Id(locationId);
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
        return coursesRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Course> getFullCourses() {
        return coursesRepository.findAll().stream()
                .filter(course -> safeInt(course.getCurrentOccupancy()) >= safeInt(course.getMaxCapacity()))
                .toList();
    }

    public List<Course> getCoursesByLocationAndType(Long locationId, CourseType type) {
        validateLocationExists(locationId);
        if (type == null) throw new ValidationException("Course type is required");

        return coursesRepository.findByLocation_IdAndType(locationId, type);
    }

    public List<WaitlistEntry> getWaitlistForCourse(Long courseId) {
        getCourseById(courseId);
        return waitlistsRepository.findByCourse_IdOrderByPositionAsc(courseId);
    }

    public SignUpStatus getSignUpStatus(Long courseId, Long memberId) {
        getCourseById(courseId);
        validateMemberExists(memberId);

        SignUp signUp = signUpsRepository.findByCourse_IdAndMember_Id(courseId, memberId);
        if (signUp == null) throw new NotFoundException("No sign-up found for this member and course");

        WaitlistEntry waitlistEntry = waitlistsRepository.findBySignUp_Id(signUp.getId());

        if (waitlistEntry != null) {
            return new SignUpStatus("WAITLISTED", waitlistEntry.getPosition());
        }

        return new SignUpStatus("ENROLLED", null);
    }

    public record SignUpStatus(String status, Integer waitlistPosition) {}

    public List<Course> getAllCourses() {
        return coursesRepository.findAll();
    }

    public Course getCourseById(Long courseId) {
        if (courseId == null) throw new ValidationException("Course id is required");

        return coursesRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    public int countEnrolledMembers(Long courseId) {
        getCourseById(courseId);

        return (int) signUpsRepository.findByCourse_Id(courseId).stream()
                .filter(s -> waitlistsRepository.findBySignUp_Id(s.getId()) == null)
                .count();
    }

    public List<SignUp> getEnrolledSignUpsForCourse(Long courseId) {
        getCourseById(courseId);

        return signUpsRepository.findByCourse_Id(courseId).stream()
                .filter(s -> waitlistsRepository.findBySignUp_Id(s.getId()) == null)
                .toList();
    }

    private void validateCourseData(Course course) {
        if (course == null) throw new ValidationException("Course data is required");
        if (course.getName() == null || course.getName().isBlank()) throw new ValidationException("Course name is required");
        if (course.getType() == null) throw new ValidationException("Course type is required");
        if (course.getDayOfWeek() == null) throw new ValidationException("Course day is required");
        if (course.getStartTime() == null) throw new ValidationException("Start time is required");
        if (course.getDuration() == null || course.getDuration() <= 0) throw new ValidationException("Duration must be greater than 0");
        if (course.getMaxCapacity() == null || course.getMaxCapacity() <= 0) throw new ValidationException("Max capacity must be greater than 0");
        if (course.getTrainer() == null || course.getTrainer().getId() == null) throw new ValidationException("Trainer id is required");
        if (course.getLocation() == null || course.getLocation().getId() == null) throw new ValidationException("Location id is required");
    }

    private Trainer getTrainerById(Long trainerId) {
        if (trainerId == null) throw new ValidationException("Trainer id is required");

        User user = userRepository.findById(trainerId)
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        if (!(user instanceof Trainer trainer) || !trainer.isActive()) {
            throw new NotFoundException("Trainer not found or inactive");
        }

        return trainer;
    }

    private void validateTrainerExists(Long trainerId) {
        getTrainerById(trainerId);
    }

    private Location getLocationById(Long locationId) {
        if (locationId == null) throw new ValidationException("Location id is required");

        return locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location not found"));
    }

    private void validateLocationExists(Long locationId) {
        getLocationById(locationId);
    }

    private void validateMemberExists(Long memberId) {
        if (memberId == null) throw new ValidationException("Member id is required");
        memberService.getMemberById(memberId);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}