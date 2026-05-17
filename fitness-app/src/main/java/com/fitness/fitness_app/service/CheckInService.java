package com.fitness.fitness_app.service;

import com.fitness.fitness_app.model.CheckIn;
import com.fitness.fitness_app.model.CheckInResult;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.repository.CheckInsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckInService {
    private final CheckInsRepository checkInsRepository;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;

    public CheckInService(CheckInsRepository checkInsRepository,
                          MemberService memberService,
                          SubscriptionService subscriptionService) {
        this.checkInsRepository = checkInsRepository;
        this.memberService = memberService;
        this.subscriptionService = subscriptionService;
    }

    public CheckInResult checkInByQrCode(String qrCode, Long locationId, Long zoneId) {
        Member member;
        try {
            member = memberService.findByQrCode(qrCode);
        } catch (RuntimeException e) {
            return CheckInResult.red("Invalid QR code. Member not found.");
        }

        if (!member.isActive()) {
            return CheckInResult.red("Access denied. Member is inactive.");
        }

        if (!subscriptionService.hasValidAccess(member.getId())) {
            return CheckInResult.red("Access denied. Subscription is expired or has no remaining entries.");
        }

        if (!checkInsRepository.findOpenByMemberId(member.getId()).isEmpty()) {
            return CheckInResult.red("Access denied. Member already has an open check-in.");
        }

        subscriptionService.registerEntryUsage(member.getId());
        CheckIn checkIn = new CheckIn(null, member.getId(), locationId, zoneId, LocalDateTime.now(), null);
        checkInsRepository.add(checkIn);
        return CheckInResult.green("Access granted. Check-in registered successfully.", checkIn);
    }

    public CheckIn checkOut(Long checkInId) {
        CheckIn checkIn = checkInsRepository.findById(checkInId);
        if (checkIn == null) throw new RuntimeException("Check-in not found");
        if (checkIn.getCheckOutTime() != null) throw new RuntimeException("This check-in is already closed");
        checkIn.setCheckOutTime(LocalDateTime.now());
        checkInsRepository.update(checkIn);
        return checkIn;
    }

    public List<CheckIn> getOpenCheckInsByLocation(Long locationId) {
        return checkInsRepository.findOpenByLocationId(locationId);
    }

    public int countCurrentOccupancyByLocation(Long locationId) {
        return getOpenCheckInsByLocation(locationId).size();
    }
}
