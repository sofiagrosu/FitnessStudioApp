package com.fitness.fitness_app.service;

import com.fitness.fitness_app.model.Payment;
import com.fitness.fitness_app.model.Receipt;
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

    public PaymentService(PaymentsRepository paymentsRepository, ReceiptsRepository receiptsRepository) {
        this.paymentsRepository = paymentsRepository;
        this.receiptsRepository = receiptsRepository;
    }

    public Payment registerPayment(Long memberId, Long subscriptionId, Double amount, PaymentMethod method) {
        if (memberId == null) throw new RuntimeException("Member id is required");
        if (subscriptionId == null) throw new RuntimeException("Subscription id is required");
        if (amount == null || amount <= 0) throw new RuntimeException("Amount must be greater than 0");
        if (method == null) throw new RuntimeException("Payment method is required");

        Payment payment = new Payment(null, memberId, subscriptionId, amount, method, LocalDateTime.now());
        paymentsRepository.add(payment);
        createReceipt(payment);
        return payment;
    }

    public Receipt createReceipt(Payment payment) {
        String receiptNumber = "REC-" + payment.getId() + "-" + LocalDateTime.now().getYear();
        Receipt receipt = new Receipt(null, payment.getId(), receiptNumber, LocalDateTime.now());
        receiptsRepository.add(receipt);
        return receipt;
    }

    public List<Payment> getPaymentsForMember(Long memberId) {
        return paymentsRepository.findByMemberId(memberId);
    }

    public Receipt getReceiptForPayment(Long paymentId) {
        Receipt receipt = receiptsRepository.findByPaymentId(paymentId);
        if (receipt == null) throw new RuntimeException("Receipt not found for this payment");
        return receipt;
    }
}
