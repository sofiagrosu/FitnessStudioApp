package com.fitness.fitness_app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Location implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String address;
    private List<Zone> zones;

    public Location() {
        this.zones = new ArrayList<>();
    }

    public Location(Long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.zones = new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<Zone> getZones() { return zones; }
    public void setZones(List<Zone> zones) { this.zones = zones; }

    public void addZone(Zone zone) { this.zones.add(zone); }

    @Override public String toString() { return "Locatia " + name + " (" + address + ")"; }
}