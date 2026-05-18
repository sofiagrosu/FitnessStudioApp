package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fitness.fitness_app.model.enums.Role;
import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Admin.class, name = "ADMIN"),
        @JsonSubTypes.Type(value = Receptionist.class, name = "RECEPTIONIST"),
        @JsonSubTypes.Type(value = Trainer.class, name = "TRAINER"),
        @JsonSubTypes.Type(value = Member.class, name = "MEMBER")
})
public interface UserI extends Serializable {
    Long getId();
    Role getRole();
    String getName();
    boolean isActive();
    String getInformations();

    String getEmail();
    String getPassword();
    void setActive(boolean active);
}