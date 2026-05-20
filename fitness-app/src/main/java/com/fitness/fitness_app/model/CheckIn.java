package com.fitness.fitness_app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;
    
    @ManyToOne
@JoinColumn(name="course_id")
private Course course;

public Course getCourse(){
    return course;
}

public void setCourse(Course course){
    this.course=course;
}

public Long getCourseId(){
    return course==null ? null : course.getId();
}

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    public CheckIn() {}

    public CheckIn(Member member, Location location, Zone zone,
                   LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        this.member = member;
        this.location = location;
        this.zone = zone;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }
public CheckIn(Member member,
               Location location,
               Zone zone,
               Course course,
               LocalDateTime checkInTime,
               LocalDateTime checkOutTime) {

    this.member = member;
    this.location = location;
    this.zone = zone;
    this.course = course;
    this.checkInTime = checkInTime;
    this.checkOutTime = checkOutTime;
}
    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public Long getMemberId() {
        return member == null ? null : member.getId();
    }

    public Long getLocationId() {
        return location == null ? null : location.getId();
    }

    public Long getZoneId() {
        return zone == null ? null : zone.getId();
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

}