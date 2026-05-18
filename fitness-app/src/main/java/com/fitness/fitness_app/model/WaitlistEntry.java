package com.fitness.fitness_app.model;

public class WaitlistEntry {

    public WaitlistEntry() {
    }

private Long id;
private Long courseId;
private Long memberId;
private Long signUpId;
private Integer position;

public WaitlistEntry(Long id, Long signUpId, Long memberId, Long courseId, Integer position) {
    this.id = id;
    this.courseId = courseId;
    this.memberId = memberId;
    this.signUpId = signUpId;
    this.position = position;
}
//to string : get all informations (except id) in a string
@Override
public String toString() {
    return "WaitlistEntry{" +
            "courseId=" + courseId +
            ", memberId=" + memberId +
            ", signUpId=" + signUpId +
            ", position=" + position +
            '}';
}
public Long getCourseId() {
    return courseId;
}

public void setCourseId(Long courseId) {
    this.courseId = courseId;
}
public Long getId() {
    return id;
}
public void setId(Long id) {
    this.id = id;
}
public Long getMemberId() {
    return memberId;
}
public void setMemberId(Long memberId) {
    this.memberId = memberId;
}
public Long getSignUpId() {
    return signUpId;
}
public void setSignUpId(Long signUpId) {
    this.signUpId = signUpId;
}
public Integer getPosition() {
    return position;
}
public void setPosition(Integer position) {
    this.position = position;
}
}

