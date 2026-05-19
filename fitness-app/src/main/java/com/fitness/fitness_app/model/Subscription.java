package com.fitness.fitness_app.model;

import com.fitness.fitness_app.model.enums.SubscriptionStatus;
import com.fitness.fitness_app.model.enums.SubscriptionType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private SubscriptionType type;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer remainingEntries;

    private Double price;

    private boolean paid=false;

    public Subscription(){}

    public Subscription(Member member,
                        SubscriptionType type,
                        LocalDate startDate,
                        Double price){

        this.member=member;
        this.type=type;
        this.startDate=startDate;
        this.price=price;
        this.status=SubscriptionStatus.ACTIVE;
    }

    public Long getId(){return id;}

    public Member getMember(){return member;}
    public void setMember(Member member){this.member=member;}

    public Long getMemberId(){
        return member==null ? null : member.getId();
    }

    public SubscriptionType getType(){return type;}
    public void setType(SubscriptionType type){this.type=type;}

    public SubscriptionStatus getStatus(){return status;}
    public void setStatus(SubscriptionStatus status){this.status=status;}

    public LocalDate getStartDate(){return startDate;}
    public void setStartDate(LocalDate startDate){this.startDate=startDate;}

    public LocalDate getEndDate(){return endDate;}
    public void setEndDate(LocalDate endDate){this.endDate=endDate;}

    public Integer getRemainingEntries(){return remainingEntries;}
    public void setRemainingEntries(Integer remainingEntries){this.remainingEntries=remainingEntries;}

    public Double getPrice(){return price;}
    public void setPrice(Double price){this.price=price;}

    public boolean isPaid(){return paid;}
    public void setPaid(boolean paid){this.paid=paid;}
}