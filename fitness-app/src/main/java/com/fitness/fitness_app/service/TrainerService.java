package com.fitness.fitness_app.service;

import com.fitness.fitness_app.model.Trainer;
import com.fitness.fitness_app.repository.TrainersRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerService {
    private final TrainersRepository trainersRepository;

    public TrainerService(TrainersRepository trainersRepository) {
        this.trainersRepository = trainersRepository;
    }

    public List<Trainer> getAllTrainers() {
        return trainersRepository.findAll();
    }

    public Trainer getTrainerById(Long trainerId) {
        Trainer trainer = trainersRepository.findById(trainerId);
        if (trainer == null) {
            throw new RuntimeException("Trainer not found");
        }
        return trainer;
    }

    public Trainer createTrainer(Trainer trainer) {
        if (trainer == null) {
            throw new RuntimeException("Trainer data is required");
        }
        if (trainer.getEmail() == null || trainer.getEmail().isBlank()) {
            throw new RuntimeException("Trainer email is required");
        }
        if (trainersRepository.findByEmail(trainer.getEmail()) != null) {
            throw new RuntimeException("Trainer email already exists");
        }
        trainersRepository.save(trainer);
        return trainer;
    }

    public Trainer updateTrainer(Long trainerId, Trainer updatedTrainer) {
        Trainer existingTrainer = getTrainerById(trainerId);
        updatedTrainer.setId(existingTrainer.getId());
        trainersRepository.save(updatedTrainer);
        return updatedTrainer;
    }

    public void deactivateTrainer(Long trainerId) {
        trainersRepository.deleteById(trainerId);
    }
}
