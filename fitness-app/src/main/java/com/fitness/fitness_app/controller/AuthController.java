package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.model.UserI;
import com.fitness.fitness_app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserI> login(@RequestBody LoginRequest request) {
        UserI user = authService.login(request.email(), request.password());
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<Member> register(@RequestBody Member member) {
        return ResponseEntity.ok(authService.register(member));
    }

    public record LoginRequest(String email, String password) {}
}
