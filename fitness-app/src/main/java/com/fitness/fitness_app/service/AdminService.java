package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.ForbiddenException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.Receptionist;
import com.fitness.fitness_app.model.Trainer;
import com.fitness.fitness_app.model.User;
import com.fitness.fitness_app.model.UserI;
import com.fitness.fitness_app.model.enums.Role;
import com.fitness.fitness_app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    private final UserRepository userRepo;

    public AdminService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public UserI addUser(UserI newUser) {
        if (newUser == null) throw new ValidationException("User data is required");

        if (!(newUser instanceof User userToSave)) {
            throw new ValidationException("Invalid user type");
        }

        if (newUser.getRole() == Role.MEMBER) {
            throw new ForbiddenException("Members cannot be added through admin. Use POST /auth/register instead.");
        }

        if (newUser.getEmail() == null || newUser.getEmail().isBlank())
            throw new ValidationException("Email is required");

        if (newUser.getPassword() == null || newUser.getPassword().isBlank())
            throw new ValidationException("Password is required");

        User existing = userRepo.findByEmailIgnoreCase(newUser.getEmail());

        if (existing != null) {
            if (existing.isActive()) {
                throw new ConflictException("This email is already registered by an active user");
            }

            existing.setActive(true);
            return userRepo.save(existing);
        }

        return userRepo.save(userToSave);
    }

    public void deactivateUser(Long id) {
        if (id == null) throw new ValidationException("User id is required");

        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Admins cannot be deactivated through this endpoint");
        }

        user.setActive(false);
        userRepo.save(user);
    }

    public List<UserI> getAllActiveUsers() {
        return userRepo.findAll().stream()
                .filter(User::isActive)
                .map(user -> (UserI) user)
                .toList();
    }

    public List<UserI> getAllUsers() {
        return userRepo.findAll().stream()
                .map(user -> (UserI) user)
                .toList();
    }

    public List<Trainer> getAllTrainers() {
        return userRepo.findAll().stream()
                .filter(u -> u instanceof Trainer)
                .map(u -> (Trainer) u)
                .toList();
    }

    public Trainer getTrainerById(Long id) {
        if (id == null) throw new ValidationException("Trainer id is required");

        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        if (!(user instanceof Trainer trainer)) {
            throw new NotFoundException("Trainer not found");
        }

        return trainer;
    }

    public Trainer updateTrainer(Long id, Trainer updatedTrainer) {
        Trainer existing = getTrainerById(id);

        if (updatedTrainer == null) throw new ValidationException("Trainer data is required");

        if (updatedTrainer.getFirstName() == null || updatedTrainer.getFirstName().isBlank())
            throw new ValidationException("First name is required");

        if (updatedTrainer.getLastName() == null || updatedTrainer.getLastName().isBlank())
            throw new ValidationException("Last name is required");

        if (updatedTrainer.getEmail() == null || updatedTrainer.getEmail().isBlank())
            throw new ValidationException("Email is required");

        if (!updatedTrainer.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepo.findByEmailIgnoreCase(updatedTrainer.getEmail()) != null)
                throw new ConflictException("Email already used by another user");
        }

        existing.setFirstName(updatedTrainer.getFirstName());
        existing.setLastName(updatedTrainer.getLastName());
        existing.setEmail(updatedTrainer.getEmail());

        if (updatedTrainer.getPassword() != null && !updatedTrainer.getPassword().isBlank()) {
            existing.setPassword(updatedTrainer.getPassword());
        }

        return userRepo.save(existing);
    }

    public List<Receptionist> getAllReceptionists() {
        return userRepo.findAll().stream()
                .filter(u -> u instanceof Receptionist)
                .map(u -> (Receptionist) u)
                .toList();
    }

    public Receptionist getReceptionistById(Long id) {
        if (id == null) throw new ValidationException("Receptionist id is required");

        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Receptionist not found"));

        if (!(user instanceof Receptionist receptionist)) {
            throw new NotFoundException("Receptionist not found");
        }

        return receptionist;
    }

    public Receptionist updateReceptionist(Long id, Receptionist updatedReceptionist) {
        Receptionist existing = getReceptionistById(id);

        if (updatedReceptionist == null) throw new ValidationException("Receptionist data is required");

        if (updatedReceptionist.getFirstName() == null || updatedReceptionist.getFirstName().isBlank())
            throw new ValidationException("First name is required");

        if (updatedReceptionist.getLastName() == null || updatedReceptionist.getLastName().isBlank())
            throw new ValidationException("Last name is required");

        if (updatedReceptionist.getEmail() == null || updatedReceptionist.getEmail().isBlank())
            throw new ValidationException("Email is required");

        if (!updatedReceptionist.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepo.findByEmailIgnoreCase(updatedReceptionist.getEmail()) != null)
                throw new ConflictException("Email already used by another user");
        }

        existing.setFirstName(updatedReceptionist.getFirstName());
        existing.setLastName(updatedReceptionist.getLastName());
        existing.setEmail(updatedReceptionist.getEmail());

        if (updatedReceptionist.getPassword() != null && !updatedReceptionist.getPassword().isBlank()) {
            existing.setPassword(updatedReceptionist.getPassword());
        }

        return userRepo.save(existing);
    }
}