package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.Payment;
import com.fitness.fitness_app.model.Receipt;
import com.fitness.fitness_app.model.enums.PaymentMethod;
import com.fitness.fitness_app.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentsController {
    private final PaymentService paymentService;

    public PaymentsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> registerPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.registerPayment(
                request.memberId(),
                request.subscriptionId(),
                request.amount(),
                request.method()
        ));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Payment>> getPaymentsForMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(paymentService.getPaymentsForMember(memberId));
    }

    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<Receipt> getReceiptForPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getReceiptForPayment(paymentId));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    public record PaymentRequest(Long memberId, Long subscriptionId, Double amount, PaymentMethod method) {}
}
