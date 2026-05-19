package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmailIgnoreCase(String email);
    Member findByQrCode(String qrCode);
}