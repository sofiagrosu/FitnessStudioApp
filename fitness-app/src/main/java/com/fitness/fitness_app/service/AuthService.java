package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.model.User;
import com.fitness.fitness_app.model.UserI;
import com.fitness.fitness_app.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final MemberService memberService;

    public AuthService(UserRepository userRepo, MemberService memberService) {
        this.userRepo = userRepo;
        this.memberService = memberService;
    }

    /**
     * Incearca autentificarea. Returneaza userul logat sau null daca credentialele sunt gresite.
     * Controllerul returneaza 401 cand rezultatul e null.
     * Cauta userul in tabela users. Pentru ca Member extinde User, membrii sunt inclusi aici.
     */
    public UserI login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        User user = userRepo.findByEmailIgnoreCase(email);

        if (user != null
                && user.getPassword() != null
                && user.getPassword().equals(password)
                && user.isActive()) {
            return user;
        }

        return null;
    }

    public Member register(Member member) {
        if (member == null) {
            throw new ValidationException("Member data is required");
        }

        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new ValidationException("Email is required");
        }

        if (member.getPassword() == null || member.getPassword().isBlank()) {
            throw new ValidationException("Password is required");
        }

        if (userRepo.findByEmailIgnoreCase(member.getEmail()) != null) {
            throw new ConflictException("Email already registered");
        }

        return memberService.registerMember(member);
    }
}