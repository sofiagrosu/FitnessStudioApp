package com.fitness.fitness_app.model;

import com.fitness.fitness_app.model.enums.PaymentMethod;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private LocalDateTime paymentDate;

    public Payment() {}

    public Payment(Member member, Subscription subscription, Double amount, PaymentMethod method, LocalDateTime paymentDate) {
        this.member = member;
        this.subscription = subscription;
        this.amount = amount;
        this.method = method;
        this.paymentDate = paymentDate;
    }

    public Long getId() { return id; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public Long getMemberId() {
        return member == null ? null : member.getId();
    }

    public Long getSubscriptionId() {
        return subscription == null ? null : subscription.getId();
    }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}