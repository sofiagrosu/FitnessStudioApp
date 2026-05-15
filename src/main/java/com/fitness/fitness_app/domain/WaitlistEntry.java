package com.fitness.fitness_app.domain;

public class WaitlistEntry {
private Long id;
private Long SignUpId;
private Integer position;
public WaitlistEntry(Long id, Long signUpId, Integer position) {
    this.id = id;
    SignUpId = signUpId;
    this.position = position;

}
public Long getId() {
    return id;
}
public void setId(Long id) {
    this.id = id;
}
public Long getSignUpId() {
    return SignUpId;
}
public void setSignUpId(Long signUpId) {
    SignUpId = signUpId;
}
public Integer getPosition() {
    return position;
}
public void setPosition(Integer position) {
    this.position = position;
}
}

