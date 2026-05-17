package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.Subscription;
import com.fitness.fitness_app.model.enums.SubscriptionType;
import com.fitness.fitness_app.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@CrossOrigin(origins = "http://localhost:3000")
public class SubscriptionsController {
    private final SubscriptionService subscriptionService;

    public SubscriptionsController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(@RequestBody CreateSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscription(request.memberId(), request.type(), request.price()));
    }

    @PutMapping("/{subscriptionId}/renew")
    public ResponseEntity<Subscription> renewSubscription(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService.renewSubscription(subscriptionId));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Subscription>> getSubscriptionsForMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsForMember(memberId));
    }

    @GetMapping("/member/{memberId}/active")
    public ResponseEntity<Subscription> getActiveSubscriptionForMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptionForMember(memberId));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    public record CreateSubscriptionRequest(Long memberId, SubscriptionType type, Double price) {}
}
