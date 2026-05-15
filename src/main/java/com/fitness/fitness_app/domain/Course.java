package com.fitness.fitness_app.domain;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class Course {

    private Long id;
    private Long trainerId; 
    private String name;
    private CourseType type;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private Integer duration; // in minutes
    private Integer maxCapacity;
    private Boolean recurring;
    private Integer currentOccupancy; // New field to track current occupancy
    private Long locationId; // New field to associate a location with the course
  
    // Constructor complet
    public Course(Long id, Long trainerId, String name, CourseType type,
                  DayOfWeek dayOfWeek, LocalTime startTime,
                  Integer duration, Integer maxCapacity,
                  Boolean recurring, Integer currentOccupancy, Long locationId) {

        this.id = id;
        this.trainerId = trainerId;
        this.name = name;
        this.type = type;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.duration = duration;
        this.maxCapacity = maxCapacity;
        this.recurring = recurring;
        this.currentOccupancy = currentOccupancy;
        this.locationId = locationId;
    }

public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        this.type = type;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return startTime.plusMinutes(duration);
    }


    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(Boolean recurring) {
        this.recurring = recurring;
    }
public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }
public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}


