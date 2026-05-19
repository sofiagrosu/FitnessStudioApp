package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.CheckIn;
import com.fitness.fitness_app.model.CheckInResult;
import com.fitness.fitness_app.model.Location;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.model.Zone;
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

    /**
     * Inregistreaza un check-in prin scanarea QR code-ului.
     * Returneaza CheckInResult (GREEN/RED) — nu arunca exceptii pentru validari,
     * deoarece rezultatul este afisat pe un ecran fizic de acces.
     */
    public CheckInResult checkInByQrCode(String qrCode, Long locationId, Long zoneId) {
        if (qrCode == null || qrCode.isBlank()) {
            return CheckInResult.red("Invalid QR code. QR code is required.");
        }

        Location location;
        try {
            location = validateLocationAndZone(locationId, zoneId);
        } catch (RuntimeException e) {
            return CheckInResult.red(e.getMessage());
        }

        Member member;
        try {
            member = memberService.findByQrCode(qrCode);
        } catch (RuntimeException e) {
            return CheckInResult.red("Invalid QR code. Member not found.");
        }

        // Nota: member.isActive() este intotdeauna true (membrii nu pot fi dezactivati).
        // Accesul fizic este controlat exclusiv de abonament.

        if (!subscriptionService.hasValidAccess(member.getId())) {
            return CheckInResult.red("Access denied. Subscription is expired or has no remaining entries.");
        }

        if (!checkInsRepository.findOpenByMemberId(member.getId()).isEmpty()) {
            return CheckInResult.red("Access denied. Member already has an open check-in.");
        }

        subscriptionService.registerEntryUsage(member.getId());
        CheckIn checkIn = new CheckIn(null, member.getId(), location.getId(), zoneId, LocalDateTime.now(), null);
        checkInsRepository.save(checkIn);
        return CheckInResult.green("Access granted. Check-in registered successfully.", checkIn);
    }

    public CheckIn checkOut(Long checkInId) {
        if (checkInId == null) throw new ValidationException("Check-in id is required");
        CheckIn checkIn = checkInsRepository.findById(checkInId);
        if (checkIn == null) throw new NotFoundException("Check-in not found");
        if (checkIn.getCheckOutTime() != null) throw new ConflictException("This check-in is already closed");
        checkIn.setCheckOutTime(LocalDateTime.now());
        checkInsRepository.save(checkIn);
        return checkIn;
    }

    public List<CheckIn> getOpenCheckInsByLocation(Long locationId) {
        validateLocation(locationId);
        return checkInsRepository.findOpenByLocationId(locationId);
    }

    public int countCurrentOccupancyByLocation(Long locationId) {
        return getOpenCheckInsByLocation(locationId).size();
    }

    public List<CheckIn> getCheckInHistoryForMember(Long memberId) {
        memberService.getMemberById(memberId); // valideaza existenta membrului
        return checkInsRepository.findAllByMemberId(memberId);
    }

    private Location validateLocationAndZone(Long locationId, Long zoneId) {
        Location location = validateLocation(locationId);
        if (zoneId == null) throw new ValidationException("Zone id is required");
        boolean zoneExists = location.getZones() != null && location.getZones().stream()
                .anyMatch(zone -> zoneId.equals(zone.getId()));
        if (!zoneExists) throw new NotFoundException("Zone not found in this location");
        return location;
    }

   private Location validateLocation(Long locationId) {
    if (locationId == null) {
        throw new ValidationException("Location id is required");
    }

    return locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException("Location not found"));
}
}