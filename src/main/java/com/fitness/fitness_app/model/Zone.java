package com.fitness.fitness_app.model;

import java.io.Serializable;

public class Zone implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private int maxCapacity;

    public Zone() {}

    public Zone(Long id, String name, int maxCapacity) {
        this.id = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    @Override public String toString() { return "Zona: " + name + " (Capacitate: " + maxCapacity + ")"; }
}