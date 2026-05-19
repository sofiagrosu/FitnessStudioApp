package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "waitlist_entries")
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sign_up_id", nullable = false)
    private SignUp signUp;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private Integer position;

    public WaitlistEntry() {}

    public WaitlistEntry(SignUp signUp, Member member, Course course, Integer position) {
        this.signUp = signUp;
        this.member = member;
        this.course = course;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public SignUp getSignUp() {
        return signUp;
    }

    public void setSignUp(SignUp signUp) {
        this.signUp = signUp;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @JsonIgnore
    public Long getSignUpId() {
        return signUp == null ? null : signUp.getId();
    }

    @JsonIgnore
    public Long getMemberId() {
        return member == null ? null : member.getId();
    }

    @JsonIgnore
    public Long getCourseId() {
        return course == null ? null : course.getId();
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "WaitlistEntry{" +
                "courseId=" + getCourseId() +
                ", memberId=" + getMemberId() +
                ", signUpId=" + getSignUpId() +
                ", position=" + position +
                '}';
    }
}