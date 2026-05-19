package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.Trainer;
import com.fitness.fitness_app.repository.TrainerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerService {

    private final TrainerRepository trainersRepository;

    public TrainerService(TrainerRepository trainersRepository) {
        this.trainersRepository = trainersRepository;
    }

    public List<Trainer> getAllTrainers() {
        return trainersRepository.findAll();
    }

    public Trainer getTrainerById(Long trainerId) {
        if (trainerId == null) {
            throw new ValidationException("Trainer id is required");
        }

        return trainersRepository.findById(trainerId)
                .orElseThrow(() -> new NotFoundException("Trainer not found"));
    }

    public Trainer createTrainer(Trainer trainer) {
        if (trainer == null) {
            throw new ValidationException("Trainer data is required");
        }

        if (trainer.getEmail() == null || trainer.getEmail().isBlank()) {
            throw new ValidationException("Trainer email is required");
        }

        if (trainersRepository.findByEmailIgnoreCase(trainer.getEmail()) != null) {
            throw new ConflictException("Trainer email already exists");
        }

        trainer.setActive(true);

        return trainersRepository.save(trainer);
    }

    public Trainer updateTrainer(Long trainerId, Trainer updatedTrainer) {
        Trainer existingTrainer = getTrainerById(trainerId);

        if (updatedTrainer == null) {
            throw new ValidationException("Trainer data is required");
        }

        existingTrainer.setFirstName(updatedTrainer.getFirstName());
        existingTrainer.setLastName(updatedTrainer.getLastName());
        existingTrainer.setEmail(updatedTrainer.getEmail());

        if (updatedTrainer.getPassword() != null
                && !updatedTrainer.getPassword().isBlank()) {
            existingTrainer.setPassword(updatedTrainer.getPassword());
        }

        return trainersRepository.save(existingTrainer);
    }

    public void deactivateTrainer(Long trainerId) {
        Trainer trainer = getTrainerById(trainerId);

        trainer.setActive(false);

        trainersRepository.save(trainer);
    }
}