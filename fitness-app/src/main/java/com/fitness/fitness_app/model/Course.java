package com.fitness.fitness_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name="courses")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="trainer_id")
    private Trainer trainer;

    @ManyToOne
    @JoinColumn(name="location_id")
    private Location location;

    private String name;

    @Enumerated(EnumType.STRING)
    private CourseType type;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;

    private Integer duration;

    private Integer maxCapacity;

    private Boolean recurring;

    private Integer currentOccupancy;

    public Course(){}

    public Long getId() {
        return id;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type){
        this.type=type;
    }

    public DayOfWeek getDayOfWeek(){
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek){
        this.dayOfWeek=dayOfWeek;
    }

    public LocalTime getStartTime(){
        return startTime;
    }

    public void setStartTime(LocalTime startTime){
        this.startTime=startTime;
    }

    public Integer getDuration(){
        return duration;
    }

    public void setDuration(Integer duration){
        this.duration=duration;
    }

    public Integer getMaxCapacity(){
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity){
        this.maxCapacity=maxCapacity;
    }

    public Boolean getRecurring(){
        return recurring;
    }

    public void setRecurring(Boolean recurring){
        this.recurring=recurring;
    }

    public Integer getCurrentOccupancy(){
        return currentOccupancy;
    }

    public void setCurrentOccupancy(Integer currentOccupancy){
        this.currentOccupancy=currentOccupancy;
    }

    @JsonIgnore
    public LocalTime getEndTime() {
        if(startTime==null || duration==null){
            return null;
        }
        return startTime.plusMinutes(duration);
    }
}