package com.fitness.fitness_app.model;

import com.fitness.fitness_app.model.enums.SubscriptionStatus;
import com.fitness.fitness_app.model.enums.SubscriptionType;
import java.time.LocalDate;

public class Subscription {
    private Long id;
    private Long memberId;
    private SubscriptionType type;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer remainingEntries;
    private Double price;
    private boolean paid = false;

    public Subscription() {}

    public Subscription(Long id, Long memberId, SubscriptionType type, LocalDate startDate, Double price) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.startDate = startDate;
        this.price = price;
        this.status = SubscriptionStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "memberId=" + memberId +
                ", type=" + type +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", remainingEntries=" + remainingEntries +
                ", price=" + price +
                '}';
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public SubscriptionType getType() { return type; }
    public void setType(SubscriptionType type) { this.type = type; }
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getRemainingEntries() { return remainingEntries; }
    public void setRemainingEntries(Integer remainingEntries) { this.remainingEntries = remainingEntries; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}
