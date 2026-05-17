package com.fitness.fitness_app.model;

import java.time.LocalDateTime;

public class Receipt {
    private Long id;
    private Long paymentId;
    private String receiptNumber;
    private LocalDateTime issuedAt;

    public Receipt() {}

    public Receipt(Long id, Long paymentId, String receiptNumber, LocalDateTime issuedAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.receiptNumber = receiptNumber;
        this.issuedAt = issuedAt;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "paymentId=" + paymentId +
                ", receiptNumber='" + receiptNumber + '\'' +
                ", issuedAt=" + issuedAt +
                '}';
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
