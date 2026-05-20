package com.fitness.fitness_app.service;

import com.fitness.fitness_app.controller.CheckInsController;
import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.CheckIn;
import com.fitness.fitness_app.model.CheckInResult;
import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.model.Location;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.model.SignUp;
import com.fitness.fitness_app.model.Zone;
import com.fitness.fitness_app.repository.CheckInsRepository;
import com.fitness.fitness_app.repository.LocationRepository;
import com.fitness.fitness_app.repository.SignUpsRepository;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckInService {

    private final CheckInsRepository checkInsRepository;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final LocationRepository locationRepository;
    private final CoursesService coursesService;
private final SignUpsRepository signUpsRepository;

    public CheckInService(CheckInsRepository checkInsRepository,
                          MemberService memberService,
                          SubscriptionService subscriptionService,
                          LocationRepository locationRepository,
                          CoursesService coursesService,
                          SignUpsRepository signUpsRepository) {
        this.checkInsRepository = checkInsRepository;
        this.memberService = memberService;
        this.subscriptionService = subscriptionService;
        this.locationRepository = locationRepository;
        this.coursesService = coursesService;
        this.signUpsRepository = signUpsRepository;
    }
public CheckInsController.MemberCheckInInfoResponse
getMemberInfoByUniqueCode(String uniqueCode){

    Member member=memberService.findByQrCode(uniqueCode);

    List<Course> courses=
            coursesService.getCoursesForMember(member.getId());

    List<CheckInsController.ClassReservationResponse>
            reservations=

            courses.stream()
                    .map(course ->
                            new CheckInsController.ClassReservationResponse(
                                    course.getId(),
                                    course.getId(),
                                    course.getName(),
                                    course.getTrainer().getFirstName()
                                            +" "
                                            +course.getTrainer().getLastName(),
                                    course.getDayOfWeek().toString(),
                                    course.getStartTime().toString(),
                                    course.getStartTime()
                                            .plusMinutes(course.getDuration())
                                            .toString(),
                                    course.getLocation().getName()
                            ))
                    .toList();

    return new CheckInsController
            .MemberCheckInInfoResponse(
                    member.getId(),
                    member.getFirstName(),
                    member.getLastName(),
                    member.getQrCode(),
                    reservations
            );
}
public CheckInResult checkInToClass(
        Long memberId,
        Long reservationId
){

    Member member=
            memberService.getMemberById(memberId);

    SignUp reservation=
            signUpsRepository.findById(reservationId)
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Reservation not found"));

    if(!reservation.getMemberId().equals(memberId)){
        return CheckInResult.red(
                "Reservation does not belong to member");
    }

    if(!subscriptionService.hasValidAccess(memberId)){
        return CheckInResult.red(
                "Subscription invalid");
    }

    if(!checkInsRepository
            .findByMember_IdAndCheckOutTimeIsNull(memberId)
            .isEmpty()){

        return CheckInResult.red(
                "Member already checked in");
    }

    Course course=
            reservation.getCourse();

    CheckIn checkIn=
            new CheckIn(
                    member,
                    course.getLocation(),
                    null,
                    course,
                    LocalDateTime.now(),
                    null
            );

    CheckIn saved=
            checkInsRepository.save(checkIn);

    return CheckInResult.green(
            "Check-in successful",
            saved
    );
}
public CheckInResult checkInToFitnessZone(
        Long memberId,
        Long locationId,
        Long zoneId
){

    Member member=
            memberService.getMemberById(memberId);

    Location location=
            validateLocation(locationId);

    Zone zone=
            location.getZones()
                    .stream()
                    .filter(z ->
                            z.getId().equals(zoneId))
                    .findFirst()
                    .orElseThrow();

    if(!subscriptionService.hasValidAccess(memberId)){
        return CheckInResult.red(
                "Subscription invalid");
    }

    CheckIn checkIn=
            new CheckIn(
                    member,
                    location,
                    zone,
                    null,
                    LocalDateTime.now(),
                    null
            );

    CheckIn saved=
            checkInsRepository.save(checkIn);

    return CheckInResult.green(
            "Fitness check-in successful",
            saved
    );
}
    public CheckInResult checkInByQrCode(String qrCode,
                                         Long locationId,
                                         Long zoneId) {

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

        Zone zone = location.getZones()
                .stream()
                .filter(z -> zoneId.equals(z.getId()))
                .findFirst()
                .orElseThrow();

        subscriptionService.registerEntryUsage(member.getId());

        CheckIn checkIn = new CheckIn(
                member,
                location,
                zone,
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
                .orElseThrow(() ->
                        new NotFoundException("Check-in not found"));

        if (checkIn.getCheckOutTime() != null) {
            throw new ConflictException(
                    "This check-in is already closed"
            );
        }

        checkIn.setCheckOutTime(LocalDateTime.now());

        return checkInsRepository.save(checkIn);
    }

    public List<CheckIn> getOpenCheckInsByLocation(Long locationId) {

        validateLocation(locationId);

        return checkInsRepository.findByLocation_Id(locationId)
                .stream()
                .filter(c -> c.getCheckOutTime()==null)
                .toList();
    }

    public int countCurrentOccupancyByLocation(Long locationId) {
        return getOpenCheckInsByLocation(locationId).size();
    }

    public List<CheckIn> getCheckInHistoryForMember(Long memberId) {

        memberService.getMemberById(memberId);

        return checkInsRepository.findByMember_Id(memberId);
    }

    private Location validateLocationAndZone(Long locationId,
                                             Long zoneId) {

        Location location = validateLocation(locationId);

        if (zoneId == null) {
            throw new ValidationException(
                    "Zone id is required"
            );
        }

        boolean zoneExists =
                location.getZones()!=null &&
                location.getZones()
                        .stream()
                        .anyMatch(
                                z -> zoneId.equals(z.getId())
                        );

        if (!zoneExists) {
            throw new NotFoundException(
                    "Zone not found in this location"
            );
        }

        return location;
    }

    private Location validateLocation(Long locationId) {

        if (locationId == null) {
            throw new ValidationException(
                    "Location id is required"
            );
        }

        return locationRepository
                .findById(locationId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Location not found"
                        ));
    }
}