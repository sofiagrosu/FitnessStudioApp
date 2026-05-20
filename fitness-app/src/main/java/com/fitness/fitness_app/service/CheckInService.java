package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.CheckIn;
import com.fitness.fitness_app.model.CheckInResult;
import com.fitness.fitness_app.model.Location;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.repository.CheckInsRepository;
import com.fitness.fitness_app.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckInService {

    private final CheckInsRepository checkInsRepository;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final LocationRepository locationRepository;

    public CheckInService(CheckInsRepository checkInsRepository,
                          MemberService memberService,
                          SubscriptionService subscriptionService,
                          LocationRepository locationRepository) {
        this.checkInsRepository = checkInsRepository;
        this.memberService = memberService;
        this.subscriptionService = subscriptionService;
        this.locationRepository = locationRepository;
    }

    public CheckInResult checkInByQrCode(String qrCode, Long locationId) {

        if (qrCode == null || qrCode.isBlank()) {
            return CheckInResult.red("Invalid QR code. QR code is required.");
        }

        Location location;
        try {
            location = validateLocation(locationId);
        } catch (RuntimeException e) {
            return CheckInResult.red(e.getMessage());
        }

        Member member;
        try {
            member = memberService.findByQrCode(qrCode);
        } catch (RuntimeException e) {
            return CheckInResult.red("Invalid QR code. Member not found.");
        }

        if (!subscriptionService.hasValidAccess(member.getId())) {
            return CheckInResult.red(
                    "Access denied. Subscription is expired or has no remaining entries."
            );
        }

        if (!checkInsRepository
                .findByMember_IdAndCheckOutTimeIsNull(member.getId())
                .isEmpty()) {
            return CheckInResult.red(
                    "Access denied. Member already has an open check-in."
            );
        }

        subscriptionService.registerEntryUsage(member.getId());

        CheckIn checkIn = new CheckIn(
                member,
                location,
                LocalDateTime.now(),
                null
        );

        CheckIn saved = checkInsRepository.save(checkIn);

        return CheckInResult.green(
                "Access granted. Check-in registered successfully.",
                saved
        );
    }

    public CheckIn checkOut(Long checkInId) {

        if (checkInId == null) {
            throw new ValidationException("Check-in id is required");
        }

        CheckIn checkIn = checkInsRepository.findById(checkInId)
                .orElseThrow(() -> new NotFoundException("Check-in not found"));

        if (checkIn.getCheckOutTime() != null) {
            throw new ConflictException("This check-in is already closed");
        }

        checkIn.setCheckOutTime(LocalDateTime.now());

        return checkInsRepository.save(checkIn);
    }

    public List<CheckIn> getOpenCheckInsByLocation(Long locationId) {
        validateLocation(locationId);
        return checkInsRepository.findByLocation_Id(locationId)
                .stream()
                .filter(c -> c.getCheckOutTime() == null)
                .toList();
    }

    public int countCurrentOccupancyByLocation(Long locationId) {
        return getOpenCheckInsByLocation(locationId).size();
    }

    public List<CheckIn> getCheckInHistoryForMember(Long memberId) {
        memberService.getMemberById(memberId);
        return checkInsRepository.findByMember_Id(memberId);
    }

    private Location validateLocation(Long locationId) {
        if (locationId == null) {
            throw new ValidationException("Location id is required");
        }
        return locationRepository
                .findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location not found"));
    }
}
