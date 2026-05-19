package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaitlistsRepository extends JpaRepository<WaitlistEntry, Long> {

    int MAX_WAITLIST_SIZE_PER_COURSE = 5;

    List<WaitlistEntry> findByCourse_IdOrderByPositionAsc(Long courseId);

    WaitlistEntry findBySignUp_Id(Long signUpId);

    List<WaitlistEntry> findByMember_IdOrderByPositionAsc(Long memberId);

    int countByCourse_Id(Long courseId);

    default boolean isCourseWaitlistFull(Long courseId) {
        return countByCourse_Id(courseId) >= MAX_WAITLIST_SIZE_PER_COURSE;
    }

    List<WaitlistEntry> findAllByOrderByCourse_IdAscPositionAsc();

    List<WaitlistEntry> findAllByOrderByPositionAsc();
}