package com.fitness.fitness_app.service;

import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.repository.CoursesRepository;
import com.fitness.fitness_app.repository.SignUpsRepository;
import com.fitness.fitness_app.repository.WaitlistsRepository;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fitness.fitness_app.model.SignUp;
import com.fitness.fitness_app.model.WaitlistEntry;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
public class CoursesService {

	private final CoursesRepository coursesRepository;
	private final WaitlistsRepository waitlistsRepository;
	private final SignUpsRepository signUpsRepository;

	public CoursesService(CoursesRepository coursesRepository,
						  WaitlistsRepository waitlistsRepository,
						  SignUpsRepository signUpsRepository) {
		this.coursesRepository = coursesRepository;
		this.waitlistsRepository = waitlistsRepository;
		this.signUpsRepository = signUpsRepository;
	}
	public String cancelSignUp(Long signUpId) {

    SignUp signUp = signUpsRepository.getAll()
            .stream()
            .filter(s -> s.getId().equals(signUpId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Sign-up not found"));

    updateWaitlist(signUp);

    return "Sign-up cancelled successfully";
}

//method that gets the information for all filtered courses 
public List<String> getAllInformationForFilteredCourses(List<Course> filteredCourses) {
		return filteredCourses.stream()
				.map(Course::toString)
				 .toList();
	}
	
//method that gets the information for all courses 
	public List<String> getAllInformationForAllCourses() {
		return coursesRepository.getAll().stream()
				.map(Course::toString)	
				.toList();
	}

private void updateWaitlist(SignUp cancelledSignUp) {

    WaitlistEntry cancelledWaitlistEntry =
            waitlistsRepository.findBySignUpId(cancelledSignUp.getId());

    // Case 1: the cancelled sign-up was on the waitlist
    if (cancelledWaitlistEntry != null) {
        waitlistsRepository.delete(cancelledWaitlistEntry.getId());
        signUpsRepository.delete(cancelledSignUp.getId());
        return;
    }

    // Case 2: the cancelled sign-up was an active course sign-up
    Course course = coursesRepository.findById(cancelledSignUp.getCourseId());

    if (course == null) {
        throw new RuntimeException("Course not found");
    }

    signUpsRepository.delete(cancelledSignUp.getId());

    course.setCurrentOccupancy(
            course.getCurrentOccupancy() - 1
    );

    List<WaitlistEntry> waitlistEntries =
            waitlistsRepository.findByCourseId(course.getId());

    if (!waitlistEntries.isEmpty()) {

        WaitlistEntry firstWaitlistEntry =
                waitlistEntries.stream()
                        .min(Comparator.comparing(
                                WaitlistEntry::getPosition
                        ))
                        .orElseThrow();

        waitlistsRepository.delete(firstWaitlistEntry.getId());

        course.setCurrentOccupancy(
                course.getCurrentOccupancy() + 1
        );
    }

    coursesRepository.update(course);
}

  // seteaza atributul attendence pentru un sign-up (daca membrul a participat la curs)
    public String setMemberAttendance(Long signUpId, boolean attended) {
        SignUp signUp = signUpsRepository.getAll()
                .stream()
                .filter(s -> s.getId().equals(signUpId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sign-up not found"));

        signUp.setAttended(attended);

        // attempt to persist the change
        try {
            signUpsRepository.update(signUp);
        } catch (Exception e) {
            // if update is not supported, re-add or ignore depending on repository implementation
            throw new RuntimeException("Failed to update sign-up attendance", e);
        }

        return "Attendance updated successfully";
    }

public String createSignUp(Long memberId, Long courseId) {

    Course course = coursesRepository.findById(courseId);

    if (course == null) {
        throw new RuntimeException("Course not found");
    }
    
  
    boolean courseHasAvailableSpots =
            course.getCurrentOccupancy() < course.getMaxCapacity();

    if (courseHasAvailableSpots) {

        SignUp signUp = new SignUp(
                null,
                courseId,
                memberId,
                LocalDateTime.now(),
                false
        );

        signUpsRepository.add(signUp);

        course.setCurrentOccupancy(
                course.getCurrentOccupancy() + 1
        );

        coursesRepository.update(course);

        return "Member successfully signed up for the course";
    }

    if (waitlistsRepository.isCourseWaitlistFull(courseId)) {
        throw new RuntimeException(
                "Course and waitlist are both full"
        );
    }

    SignUp signUp = new SignUp(
            null,
            courseId,
            memberId,
            LocalDateTime.now(),
            false
    );

    signUpsRepository.add(signUp);

    int waitlistPosition =
            waitlistsRepository.countByCourseId(courseId) + 1;

    WaitlistEntry waitlistEntry = new WaitlistEntry(
            null,
            signUp.getId(),
            courseId,
            waitlistPosition
    );

    waitlistsRepository.add(waitlistEntry);

    return "Course is full. Member was added to the waitlist at position "
            + waitlistPosition;
}

public String createCourse(Course course) {

    if (course == null) {
        throw new RuntimeException("Course data is required");
    }

    if (course.getMaxCapacity() <= 0) {
        throw new RuntimeException("Max capacity must be greater than 0");
    }

    if (course.getCurrentOccupancy() < 0 || course.getCurrentOccupancy() > course.getMaxCapacity()) {
        throw new RuntimeException("Invalid current occupancy");
    }

    coursesRepository.add(course);

    return "Course created successfully";
}

public String updateCourse(Long courseId, Course updatedCourse) {

    if (courseId == null) {
        throw new RuntimeException("Course id is required");
    }

    if (updatedCourse == null) {
        throw new RuntimeException("Course data is required");
    }

    Course existingCourse = coursesRepository.findById(courseId);

    if (existingCourse == null) {
        throw new RuntimeException("Course not found");
    }

    if (updatedCourse.getMaxCapacity() <= 0) {
        throw new RuntimeException("Max capacity must be greater than 0");
    }

    if (updatedCourse.getCurrentOccupancy() < 0 ||
            updatedCourse.getCurrentOccupancy() > updatedCourse.getMaxCapacity()) {
        throw new RuntimeException("Invalid current occupancy");
    }

    updatedCourse.setId(courseId);
    coursesRepository.update(updatedCourse);

    return "Course updated successfully";
}

public String deleteCourse(Long courseId) {
    Course course = coursesRepository.findById(courseId);

    if (course == null) {
        throw new RuntimeException("Course not found");
    }

    waitlistsRepository.getAll().stream()
            .filter(entry -> entry.getCourseId().equals(courseId))
            .forEach(entry -> waitlistsRepository.delete(entry.getId()));

    signUpsRepository.getAll().stream()
            .filter(signUp -> signUp.getCourseId() == courseId)
            .forEach(signUp -> signUpsRepository.delete(signUp.getId()));

    coursesRepository.delete(courseId);

    return "Course deleted successfully";
}

public List<Course> getAvailableCourses() {
    return coursesRepository.findAvailableCourses();
}

public List<Course> getCoursesByTrainer(Long trainerId) {
    return coursesRepository.findByTrainerId(trainerId);
}

public List<Course> getCoursesByTrainerAndLocation(Long trainerId, Long locationId) {
    return coursesRepository.findByTrainerId(trainerId)
            .stream()
            .filter(course -> course.getLocationId().equals(locationId))
            .toList();
}
    
    // return all courses a member is signed up for (including waitlisted)
    public List<Course> getCoursesForMember(Long memberId) {
        return signUpsRepository.getAll()
                .stream()
                .filter(s -> s.getMemberId() == memberId)
                .map(s -> coursesRepository.findById(s.getCourseId()))
                .filter(c -> c != null)
                .distinct()
                .toList();
    }

    // return all courses a member attended in the past
    public List<Course> getPastAttendedCoursesForMember(Long memberId) {
        return signUpsRepository.getAll()
                .stream()
                .filter(s -> s.getMemberId() == memberId && Boolean.TRUE.equals(s.getAttended()))
                .map(s -> coursesRepository.findById(s.getCourseId()))
                .filter(c -> c != null)
                .distinct()
                .toList();
    }

    // numara prezentele acumulate de un membru
    public long countAccumulatedAttendanceForMember(Long memberId) {
        return signUpsRepository.getAll()
                .stream()
                .filter(s -> memberId != null && s.getMemberId() == memberId && Boolean.TRUE.equals(s.getAttended()))
                .count();
    }
public List<Course> getAvailableCoursesByLocation(Long locationId) {
    return coursesRepository.findByLocationId(locationId)
            .stream()
            .filter(course -> course.getCurrentOccupancy() < course.getMaxCapacity())
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
    return coursesRepository.findByLocationId(locationId);
}
}
