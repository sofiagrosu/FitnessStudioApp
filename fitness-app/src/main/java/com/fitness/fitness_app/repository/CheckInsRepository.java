package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInsRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByMember_Id(Long memberId);

    List<CheckIn> findByLocation_Id(Long locationId);

List<CheckIn> findByMember_IdAndCheckOutTimeIsNull(Long memberId);
}