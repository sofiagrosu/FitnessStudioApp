package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.SignUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignUpsRepository extends JpaRepository<SignUp, Long> {

    List<SignUp> findByCourse_Id(Long courseId);

    List<SignUp> findByMember_Id(Long memberId);

    SignUp findByCourse_IdAndMember_Id(Long courseId, Long memberId);

    List<SignUp> findByAttendedTrue();

    List<SignUp> findByAttendedFalse();

    List<SignUp> findAllByOrderByBookingTimeAsc();

    List<SignUp> findAllByOrderByCourse_IdAsc();

    List<SignUp> findAllByOrderByMember_IdAsc();
}