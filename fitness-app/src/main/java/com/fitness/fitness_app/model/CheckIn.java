package com.fitness.fitness_app.model;

import java.time.LocalDateTime;

public class CheckIn {
    private Long id;
    private Long memberId;
    private Long locationId;
    private Long zoneId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public CheckIn() {}

    public CheckIn(Long id, Long memberId, Long locationId, Long zoneId, LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        this.id = id;
        this.memberId = memberId;
        this.locationId = locationId;
        this.zoneId = zoneId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "memberId=" + memberId +
                ", locationId=" + locationId +
                ", zoneId=" + zoneId +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                '}';
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
}
