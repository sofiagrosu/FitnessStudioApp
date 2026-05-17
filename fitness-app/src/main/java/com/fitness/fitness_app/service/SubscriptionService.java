package com.fitness.fitness_app.service;

import com.fitness.fitness_app.model.Subscription;
import com.fitness.fitness_app.model.enums.SubscriptionStatus;
import com.fitness.fitness_app.model.enums.SubscriptionType;
import com.fitness.fitness_app.repository.SubscriptionsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {
    private final SubscriptionsRepository subscriptionsRepository;

    public SubscriptionService(SubscriptionsRepository subscriptionsRepository) {
        this.subscriptionsRepository = subscriptionsRepository;
    }

    public Subscription createSubscription(Long memberId, SubscriptionType type, Double price) {
        if (memberId == null) throw new RuntimeException("Member id is required");
        if (type == null) throw new RuntimeException("Subscription type is required");

        Subscription subscription = new Subscription(null, memberId, type, LocalDate.now(), price);
        applySubscriptionRules(subscription);
        subscriptionsRepository.add(subscription);
        return subscription;
    }

    public Subscription renewSubscription(Long subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.setStartDate(LocalDate.now());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        applySubscriptionRules(subscription);
        subscriptionsRepository.update(subscription);
        return subscription;
    }

    public boolean hasValidAccess(Long memberId) {
        Subscription subscription = getActiveSubscriptionForMember(memberId);
        if (subscription == null) return false;
        refreshStatus(subscription);
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) return false;
        if (subscription.getType() == SubscriptionType.TEN_ENTRIES) {
            return subscription.getRemainingEntries() != null && subscription.getRemainingEntries() > 0;
        }
        return subscription.getEndDate() == null || !subscription.getEndDate().isBefore(LocalDate.now());
    }

    public void registerEntryUsage(Long memberId) {
        Subscription subscription = getActiveSubscriptionForMember(memberId);
        if (subscription == null) throw new RuntimeException("Member has no active subscription");
        if (subscription.getType() == SubscriptionType.TEN_ENTRIES) {
            int remainingEntries = subscription.getRemainingEntries() == null ? 0 : subscription.getRemainingEntries();
            if (remainingEntries <= 0) throw new RuntimeException("No remaining entries for this subscription");
            subscription.setRemainingEntries(remainingEntries - 1);
            if (subscription.getRemainingEntries() == 0) {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
            }
            subscriptionsRepository.update(subscription);
        }
    }

    public Subscription getActiveSubscriptionForMember(Long memberId) {
        Subscription subscription = subscriptionsRepository.findActiveByMemberId(memberId);
        if (subscription != null) refreshStatus(subscription);
        return subscription;
    }

    public List<Subscription> getSubscriptionsForMember(Long memberId) {
        return subscriptionsRepository.findByMemberId(memberId);
    }

    public Subscription getSubscriptionById(Long subscriptionId) {
        Subscription subscription = subscriptionsRepository.findById(subscriptionId);
        if (subscription == null) throw new RuntimeException("Subscription not found");
        return subscription;
    }

    private void applySubscriptionRules(Subscription subscription) {
        SubscriptionType type = subscription.getType();
        LocalDate startDate = subscription.getStartDate() == null ? LocalDate.now() : subscription.getStartDate();
        subscription.setStartDate(startDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        if (type == SubscriptionType.MONTHLY) {
            subscription.setEndDate(startDate.plusMonths(1));
            subscription.setRemainingEntries(null);
        } else if (type == SubscriptionType.ANNUAL) {
            subscription.setEndDate(startDate.plusYears(1));
            subscription.setRemainingEntries(null);
        } else if (type == SubscriptionType.TEN_ENTRIES) {
            subscription.setEndDate(null);
            subscription.setRemainingEntries(10);
        }
    }

    private void refreshStatus(Subscription subscription) {
        if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) return;
        boolean expiredByDate = subscription.getEndDate() != null && subscription.getEndDate().isBefore(LocalDate.now());
        boolean expiredByEntries = subscription.getType() == SubscriptionType.TEN_ENTRIES
                && subscription.getRemainingEntries() != null
                && subscription.getRemainingEntries() <= 0;
        if (expiredByDate || expiredByEntries) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionsRepository.update(subscription);
        }
    }
}
