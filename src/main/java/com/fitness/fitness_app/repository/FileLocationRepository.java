package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.Location;
import java.util.List;

public class FileLocationRepository implements RepositoryI<Location> {
    private final DataContext context = DataContext.getInstance();

    @Override
    public void save(Location location) {
        Location existing = findById(location.getId());
        if (existing != null) {
            context.getLocations().remove(existing);
        }
        context.getLocations().add(location);
        context.saveAllData();
    }

    @Override
    public Location findById(Long id) {
        return context.getLocations().stream()
                .filter(l -> l.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Location> findAll() {
        return context.getLocations();
    }

    @Override
    public void deleteById(Long id) {
        Location location = findById(id);
        if (location != null) {
            context.getLocations().remove(location);
            context.saveAllData();
        }
    }
}