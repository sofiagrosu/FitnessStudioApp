package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.CheckIn;
import com.fitness.fitness_app.model.CheckInResult;
import com.fitness.fitness_app.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/checkins")
@CrossOrigin(origins = "http://localhost:3000")
public class CheckInsController {

    private final CheckInService checkInService;

    public CheckInsController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping("/lookup")
    public ResponseEntity<MemberCheckInInfoResponse> getMemberInfoByUniqueCode(
            @RequestBody UniqueCodeRequest request
    ) {
        return ResponseEntity.ok(
                checkInService.getMemberInfoByUniqueCode(request.uniqueCode())
        );
    }

    @PostMapping("/class")
    public ResponseEntity<CheckInResult> checkInToClass(
            @RequestBody ClassCheckInRequest request
    ) {
        return ResponseEntity.ok(
                checkInService.checkInToClass(
                        request.memberId(),
                        request.reservationId()
                )
        );
    }

    @PostMapping("/fitness-zone")
    public ResponseEntity<CheckInResult> checkInToFitnessZone(
            @RequestBody FitnessZoneCheckInRequest request
    ) {
        return ResponseEntity.ok(
                checkInService.checkInToFitnessZone(
                        request.memberId(),
                        request.locationId(),
                        request.zoneId()
                )
        );
    }

    @PutMapping("/{checkInId}/checkout")
    public ResponseEntity<CheckIn> checkOut(@PathVariable Long checkInId) {
        return ResponseEntity.ok(checkInService.checkOut(checkInId));
    }

    @GetMapping("/location/{locationId}/open")
    public ResponseEntity<List<CheckIn>> getOpenCheckInsByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(checkInService.getOpenCheckInsByLocation(locationId));
    }

    @GetMapping("/location/{locationId}/occupancy")
    public ResponseEntity<Integer> countCurrentOccupancyByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(checkInService.countCurrentOccupancyByLocation(locationId));
    }

    @GetMapping("/member/{memberId}/history")
    public ResponseEntity<List<CheckIn>> getCheckInHistoryForMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(checkInService.getCheckInHistoryForMember(memberId));
    }

    public record UniqueCodeRequest(String uniqueCode) {}

    public record ClassCheckInRequest(Long memberId, Long reservationId) {}

    public record FitnessZoneCheckInRequest(Long memberId, Long locationId, Long zoneId) {}

    public record MemberCheckInInfoResponse(
            Long memberId,
            String firstName,
            String lastName,
            String uniqueCode,
            List<ClassReservationResponse> reservations
    ) {}

    public record ClassReservationResponse(
            Long reservationId,
            Long classId,
            String className,
            String trainerName,
            String date,
            String startTime,
            String endTime,
            String locationName
    ) {}
}