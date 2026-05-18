package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.Receptionist;
import com.fitness.fitness_app.model.Trainer;
import com.fitness.fitness_app.model.UserI;
import com.fitness.fitness_app.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserI>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<UserI>> getAllActiveUsers() {
        return ResponseEntity.ok(adminService.getAllActiveUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserI> addUser(@RequestBody UserI newUser) {
        return ResponseEntity.ok(adminService.addUser(newUser));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        adminService.deactivateUser(id);
        return ResponseEntity.ok("User deactivated successfully");
    }

    @GetMapping("/trainers")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        return ResponseEntity.ok(adminService.getAllTrainers());
    }

    @GetMapping("/trainers/{id}")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getTrainerById(id));
    }

    @PutMapping("/trainers/{id}")
    public ResponseEntity<Trainer> updateTrainer(@PathVariable Long id, @RequestBody Trainer trainer) {
        return ResponseEntity.ok(adminService.updateTrainer(id, trainer));
    }

    @GetMapping("/receptionists")
    public ResponseEntity<List<Receptionist>> getAllReceptionists() {
        return ResponseEntity.ok(adminService.getAllReceptionists());
    }

    @GetMapping("/receptionists/{id}")
    public ResponseEntity<Receptionist> getReceptionistById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getReceptionistById(id));
    }

    @PutMapping("/receptionists/{id}")
    public ResponseEntity<Receptionist> updateReceptionist(@PathVariable Long id, @RequestBody Receptionist receptionist) {
        return ResponseEntity.ok(adminService.updateReceptionist(id, receptionist));
    }
}
