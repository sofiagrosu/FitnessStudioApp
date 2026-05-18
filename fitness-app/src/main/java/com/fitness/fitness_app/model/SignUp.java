package com.fitness.fitness_app.model;
import java.time.LocalDateTime;

public class SignUp {

    public SignUp() {
    }

    private Long id;
    private long courseId;
    private long memberId;
  
private LocalDateTime bookingTime;
    private Boolean attended; // New field to track attendance

    public SignUp(Long id, long courseId, long memberId, LocalDateTime bookingTime, Boolean attended) {
        this.id = id;
        this.courseId = courseId;
        this.memberId = memberId;
        this.bookingTime = bookingTime;
        
        this.attended = attended;
    }

//to string : get all informations (except id) in a string
    @Override   
    public String toString() {
        return "SignUp{" +
                "courseId=" + courseId +
                ", memberId=" + memberId +
                ", bookingTime=" + bookingTime +
                ", attended=" + attended +
                '}';
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

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;

    }

    public Boolean getAttended() {
        return attended;
    }

    public void setAttended(Boolean attended) {
        this.attended = attended;
    }
   


}
