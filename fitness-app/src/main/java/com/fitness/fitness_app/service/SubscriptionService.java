package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.model.Subscription;
import com.fitness.fitness_app.model.enums.SubscriptionStatus;
import com.fitness.fitness_app.model.enums.SubscriptionType;
import com.fitness.fitness_app.repository.SignUpsRepository;
import com.fitness.fitness_app.repository.SubscriptionsRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {
    private final SubscriptionsRepository subscriptionsRepository;
    private final MemberService memberService;
    private final CoursesService coursesService;
    private final SignUpsRepository signUpsRepository;

    public SubscriptionService(SubscriptionsRepository subscriptionsRepository,
                               @Lazy MemberService memberService,
                               @Lazy CoursesService coursesService,
                               SignUpsRepository signUpsRepository) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.memberService = memberService;
        this.coursesService = coursesService;
        this.signUpsRepository = signUpsRepository;
    }

    public Subscription createSubscription(Long memberId, SubscriptionType type, Double price) {
        validateSubscriptionInput(memberId, type, price);

        Subscription active = getActiveSubscriptionForMember(memberId);
        if (active != null) {
            throw new ConflictException(
                    "Member already has an active subscription. It must expire or be suspended before purchasing a new one.");
        }

       Member member = memberService.getMemberById(memberId);

Subscription subscription =
        new Subscription(member,type,LocalDate.now(),price);
        applySubscriptionRules(subscription);
        return subscriptionsRepository.save(subscription);
    }

    public Subscription renewSubscription(Long subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        refreshStatus(subscription);

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new ConflictException("Cannot renew an already active subscription");
        }

        Subscription active = getActiveSubscriptionForMember(subscription.getMemberId());
        if (active != null && !active.getId().equals(subscription.getId())) {
            throw new ConflictException("Member already has another active subscription");
        }

        subscription.setStartDate(LocalDate.now());
        subscription.setPaid(false); // reinnoire = trebuie platita din nou
        applySubscriptionRules(subscription);
        return subscriptionsRepository.save(subscription);
    }

    public Subscription suspendSubscription(Long subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ConflictException("Only active subscriptions can be suspended");
        }
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscriptionsRepository.save(subscription);
        unenrollFromAllCourses(subscription.getMemberId());
        return subscription;
    }

    public boolean hasValidAccess(Long memberId) {
        Subscription subscription = getActiveSubscriptionForMember(memberId);
        if (subscription == null) return false;
        if (!subscription.isPaid()) return false;
        refreshStatus(subscription);
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) return false;
        if (subscription.getType() == SubscriptionType.TEN_ENTRIES) {
            return subscription.getRemainingEntries() != null && subscription.getRemainingEntries() > 0;
        }
        return subscription.getEndDate() == null || !subscription.getEndDate().isBefore(LocalDate.now());
    }

    public void registerEntryUsage(Long memberId) {
        Subscription subscription = getActiveSubscriptionForMember(memberId);
        if (subscription == null) throw new NotFoundException("Member has no active subscription");
        if (subscription.getType() == SubscriptionType.TEN_ENTRIES) {
            int remainingEntries = subscription.getRemainingEntries() == null ? 0 : subscription.getRemainingEntries();
            if (remainingEntries <= 0) throw new ConflictException("No remaining entries for this subscription");
            subscription.setRemainingEntries(remainingEntries - 1);
            if (subscription.getRemainingEntries() == 0) {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
            }
            subscriptionsRepository.save(subscription);
        }
    }
public Subscription getActiveSubscriptionForMember(Long memberId) {
    if (memberId == null) {
        return null;
    }

    Subscription subscription = subscriptionsRepository
            .findFirstByMember_IdAndStatusOrderByStartDateDesc(
                    memberId,
                    SubscriptionStatus.ACTIVE
            );

    if (subscription != null) {
        refreshStatus(subscription);
    }

    return subscription;
}

  public List<Subscription> getSubscriptionsForMember(Long memberId) {
    if (memberId == null) {
        throw new ValidationException("Member id is required");
    }

    memberService.getMemberById(memberId);

    return subscriptionsRepository.findByMember_Id(memberId);
}

    public void markAsPaid(Long subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.setPaid(true);
        subscriptionsRepository.save(subscription);
    }

   public Subscription getSubscriptionById(Long subscriptionId) {
    if (subscriptionId == null) {
        throw new ValidationException("Subscription id is required");
    }

    return subscriptionsRepository.findById(subscriptionId)
            .orElseThrow(() ->
                    new NotFoundException("Subscription not found"));
}

    private void validateSubscriptionInput(Long memberId, SubscriptionType type, Double price) {
        if (memberId == null) throw new ValidationException("Member id is required");
        memberService.getMemberById(memberId); // throws NotFoundException if member doesn't exist
        if (type == null) throw new ValidationException("Subscription type is required");
        if (price == null || price <= 0) throw new ValidationException("Price must be greater than 0");
    }

    private void applySubscriptionRules(Subscription subscription) {
        SubscriptionType type = subscription.getType();
        LocalDate startDate = subscription.getStartDate() == null ? LocalDate.now() : subscription.getStartDate();
        subscription.setStartDate(startDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        switch (type) {
            case MONTHLY -> {
                subscription.setEndDate(startDate.plusMonths(1));
                subscription.setRemainingEntries(null);
            }
            case ANNUAL -> {
                subscription.setEndDate(startDate.plusYears(1));
                subscription.setRemainingEntries(null);
            }
            case TEN_ENTRIES -> {
                subscription.setEndDate(null);
                subscription.setRemainingEntries(10);
            }
            default -> throw new ValidationException("Unsupported subscription type: " + type);
        }
    }

    private void refreshStatus(Subscription subscription) {
        if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) return;
        boolean expiredByDate = subscription.getEndDate() != null
                && subscription.getEndDate().isBefore(LocalDate.now());
        boolean expiredByEntries = subscription.getType() == SubscriptionType.TEN_ENTRIES
                && subscription.getRemainingEntries() != null
                && subscription.getRemainingEntries() <= 0;
        if (expiredByDate || expiredByEntries) {
            if (subscription.getStatus() != SubscriptionStatus.EXPIRED) {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionsRepository.save(subscription);
                unenrollFromAllCourses(subscription.getMemberId());
            }
        }
    }

    private void unenrollFromAllCourses(Long memberId) {
        signUpsRepository.findByMember_Id(memberId).forEach(signUp ->
            coursesService.cancelSignUp(signUp.getId(), memberId)
        );
    }
}
