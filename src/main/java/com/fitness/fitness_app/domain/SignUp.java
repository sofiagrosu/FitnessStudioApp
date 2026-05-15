package com.fitness.fitness_app.domain;
import java.time.LocalTime;
public class SignUp {
    private Long id;
    private long courseId;
    private long memberId;
    private LocalTime bookingTime;
    private Boolean attended; // New field to track attendance

    public SignUp(Long id, long courseId, long memberId, LocalTime bookingTime, Boolean attended) {
        this.id = id;
        this.courseId = courseId;
        this.memberId = memberId;
        this.bookingTime = bookingTime;
        this.attended = attended;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;

    }

    public Boolean getAttended() {
        return attended;
    }

    public void setAttended(Boolean attended) {
        this.attended = attended;
    }
   


}
