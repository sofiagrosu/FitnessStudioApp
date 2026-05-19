package com.fitness.fitness_app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String receiptNumber;

    private LocalDateTime issuedAt;

    public Receipt() {}

    public Receipt(Payment payment, String receiptNumber, LocalDateTime issuedAt) {
        this.payment = payment;
        this.receiptNumber = receiptNumber;
        this.issuedAt = issuedAt;
    }

    public Long getId() { return id; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public Long getPaymentId() {
        return payment == null ? null : payment.getId();
    }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}