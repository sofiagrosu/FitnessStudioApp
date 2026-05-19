package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.Trainer;
import com.fitness.fitness_app.service.TrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainers")
@CrossOrigin(origins = "http://localhost:3000")
public class TrainersController {
    private final TrainerService trainerService;

    public TrainersController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @GetMapping("/{trainerId}")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable Long trainerId) {
        return ResponseEntity.ok(trainerService.getTrainerById(trainerId));
    }

    @PostMapping
    public ResponseEntity<Trainer> createTrainer(@RequestBody Trainer trainer) {
        return ResponseEntity.ok(trainerService.createTrainer(trainer));
    }

    @PutMapping("/{trainerId}")
    public ResponseEntity<Trainer> updateTrainer(@PathVariable Long trainerId,
                                                 @RequestBody Trainer trainer) {
        return ResponseEntity.ok(trainerService.updateTrainer(trainerId, trainer));
    }

    @DeleteMapping("/{trainerId}")
    public ResponseEntity<String> deactivateTrainer(@PathVariable Long trainerId) {
        trainerService.deactivateTrainer(trainerId);
        return ResponseEntity.ok("Trainer deactivated successfully");
    }
}
