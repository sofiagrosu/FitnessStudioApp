package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sign_ups")
public class SignUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private LocalDateTime bookingTime;

    public SignUp() {}

    public SignUp(Course course, Member member, LocalDateTime bookingTime) {
        this.course = course;
        this.member = member;
        this.bookingTime = bookingTime;
    }

    public Long getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @JsonIgnore
    public Long getCourseId() {
        return course == null ? null : course.getId();
    }

    @JsonIgnore
    public Long getMemberId() {
        return member == null ? null : member.getId();
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    @Override
    public String toString() {
        return "SignUp{" +
                "courseId=" + getCourseId() +
                ", memberId=" + getMemberId() +
                ", bookingTime=" + bookingTime +
                '}';
    }
}
