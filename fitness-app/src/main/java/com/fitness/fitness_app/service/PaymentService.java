package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ForbiddenException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.Payment;
import com.fitness.fitness_app.model.Receipt;
import com.fitness.fitness_app.model.Subscription;
import com.fitness.fitness_app.model.enums.PaymentMethod;
import com.fitness.fitness_app.repository.PaymentsRepository;
import com.fitness.fitness_app.repository.ReceiptsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentsRepository paymentsRepository;
    private final ReceiptsRepository receiptsRepository;
    private final SubscriptionService subscriptionService;

    public PaymentService(PaymentsRepository paymentsRepository,
                          ReceiptsRepository receiptsRepository,
                          SubscriptionService subscriptionService) {
        this.paymentsRepository = paymentsRepository;
        this.receiptsRepository = receiptsRepository;
        this.subscriptionService = subscriptionService;
    }

    public Payment registerPayment(Long memberId, Long subscriptionId, Double amount, PaymentMethod method) {
        if (memberId == null) throw new ValidationException("Member id is required");
        if (subscriptionId == null) throw new ValidationException("Subscription id is required");
        if (amount == null || amount <= 0) throw new ValidationException("Amount must be greater than 0");
        if (method == null) throw new ValidationException("Payment method is required");

        Subscription subscription = subscriptionService.getSubscriptionById(subscriptionId);
        if (!subscription.getMemberId().equals(memberId)) {
            throw new ForbiddenException("Subscription does not belong to this member");
        }

        Payment payment = new Payment(null, memberId, subscriptionId, amount, method, LocalDateTime.now());
        paymentsRepository.save(payment);
        createReceipt(payment);
        return payment;
    }

    public Receipt createReceipt(Payment payment) {
        String receiptNumber = "REC-" + payment.getId() + "-" + LocalDateTime.now().getYear();
        Receipt receipt = new Receipt(null, payment.getId(), receiptNumber, LocalDateTime.now());
        receiptsRepository.save(receipt);
        return receipt;
    }

    public List<Payment> getPaymentsForMember(Long memberId) {
        if (memberId == null) throw new ValidationException("Member id is required");
        return paymentsRepository.findByMemberId(memberId);
    }

    public Receipt getReceiptForPayment(Long paymentId) {
        if (paymentId == null) throw new ValidationException("Payment id is required");
        Receipt receipt = receiptsRepository.findByPaymentId(paymentId);
        if (receipt == null) throw new NotFoundException("Receipt not found for this payment");
        return receipt;
    }
}
