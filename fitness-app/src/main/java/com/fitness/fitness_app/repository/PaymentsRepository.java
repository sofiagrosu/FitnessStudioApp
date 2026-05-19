package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentsRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMember_Id(Long memberId);
}