package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.Trainer;
import com.fitness.fitness_app.model.UserI;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TrainersRepository {
    private final FileUserRepository userRepository;

    public TrainersRepository(FileUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Trainer> findAll() {
        return userRepository.findAll().stream()
                .filter(Trainer.class::isInstance)
                .map(Trainer.class::cast)
                .toList();
    }

    public Trainer findById(Long id) {
        UserI user = userRepository.findById(id);
        return user instanceof Trainer trainer ? trainer : null;
    }

    public Trainer findByEmail(String email) {
        UserI user = userRepository.findByEmail(email);
        return user instanceof Trainer trainer ? trainer : null;
    }

    public void save(Trainer trainer) {
        userRepository.save(trainer);
    }

    public void deleteById(Long id) {
        Trainer trainer = findById(id);
        if (trainer == null) {
            throw new RuntimeException("Trainer not found");
        }
        userRepository.deleteById(id);
    }
}
