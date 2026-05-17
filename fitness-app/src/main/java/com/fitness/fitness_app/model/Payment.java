package com.fitness.fitness_app.model;

import com.fitness.fitness_app.model.enums.PaymentMethod;
import java.time.LocalDateTime;

public class Payment {
    private Long id;
    private Long memberId;
    private Long subscriptionId;
    private Double amount;
    private PaymentMethod method;
    private LocalDateTime paymentDate;

    public Payment() {}

    public Payment(Long id, Long memberId, Long subscriptionId, Double amount, PaymentMethod method, LocalDateTime paymentDate) {
        this.id = id;
        this.memberId = memberId;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.method = method;
        this.paymentDate = paymentDate;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "memberId=" + memberId +
                ", subscriptionId=" + subscriptionId +
                ", amount=" + amount +
                ", method=" + method +
                ", paymentDate=" + paymentDate +
                '}';
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}
