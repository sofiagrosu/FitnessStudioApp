package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Trainer findByEmailIgnoreCase(String email);
}