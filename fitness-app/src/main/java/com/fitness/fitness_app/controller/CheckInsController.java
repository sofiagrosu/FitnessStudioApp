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

    @PostMapping("/qr")
    public ResponseEntity<CheckInResult> checkInByQrCode(@RequestBody CheckInRequest request) {
        return ResponseEntity.ok(checkInService.checkInByQrCode(request.qrCode(), request.locationId(), request.zoneId()));
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    public record CheckInRequest(String qrCode, Long locationId, Long zoneId) {}
}
