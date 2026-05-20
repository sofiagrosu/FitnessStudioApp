package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.CheckIn;
import com.fitness.fitness_app.model.CheckInResult;
import com.fitness.fitness_app.service.CheckInService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @PostMapping("/qr")
    public ResponseEntity<CheckInResult> checkInByQrCode(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(checkInService.checkInByQrCode(request.qrCode(), request.locationId()));
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

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<CheckIn>> getCheckInHistoryForMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(checkInService.getCheckInHistoryForMember(memberId));
    }

    public record CheckInRequest(
            @NotBlank(message = "QR code is required")
            String qrCode,

            @NotNull(message = "Location ID is required")
            Long locationId
    ) {}
}
