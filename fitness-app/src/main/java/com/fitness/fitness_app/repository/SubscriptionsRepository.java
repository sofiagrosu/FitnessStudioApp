package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.Subscription;
import com.fitness.fitness_app.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionsRepository extends JpaRepository<Subscription,Long> {

    Subscription findFirstByMember_IdAndStatusOrderByStartDateDesc(
            Long memberId,
            SubscriptionStatus status
    );

    List<Subscription> findByMember_Id(Long memberId);
}