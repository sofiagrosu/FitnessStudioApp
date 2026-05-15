package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitness_app.domain.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LocationsRepository implements FileRepository<Location> {

    private final String filePath;
    private final ObjectMapper objectMapper;
    private List<Location> locations;

    public LocationsRepository(String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.locations = loadFromFile();
    }

    private List<Location> loadFromFile() {
        File file = new File(filePath);

        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    file,
                    new TypeReference<List<Location>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error reading locations from JSON file", e
            );
        }
    }

    @Override
    public List<Location> getAll() {
        return new ArrayList<>(locations);
    }

    @Override
    public void SaveAll(List<Location> items) {
        this.locations = new ArrayList<>(items);

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(filePath), this.locations);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error saving locations to JSON file", e
            );
        }
    }

    @Override
    public void add(Location item) {
        locations.add(item);
        SaveAll(locations);
    }

    @Override
    public void update(Location item) {

        for (int i = 0; i < locations.size(); i++) {

            if (locations.get(i)
                    .getId()
                    .equals(item.getId())) {

                locations.set(i, item);

                SaveAll(locations);

                return;
            }
        }

        throw new RuntimeException("Location not found");
    }

    @Override
    public void delete(Long id) {

        boolean removed =
                locations.removeIf(
                        location -> location.getId().equals(id)
                );

        if (!removed) {
            throw new RuntimeException("Location not found");
        }

        SaveAll(locations);
    }

    // Filters

    public List<Location> findAvailableLocations() {

        return locations.stream()
                .filter(location ->
                        location.getCurrentOccupancy()
                                < location.getCapacity())
                .toList();
    }

    public List<Location> findFullLocations() {

        return locations.stream()
                .filter(location ->
                        location.getCurrentOccupancy()
                                .equals(location.getCapacity()))
                .toList();
    }

    @Override
    public List<String> getAllInformation() {
        return locations.stream()
                .map(Location::toString)
                .toList();
    }

    public List<Location> searchByName(String keyword) {

        return locations.stream()
                .filter(location ->
                        location.getName()
                                .toLowerCase()
                                .contains(
                                        keyword.toLowerCase()))
                .toList();
    }

    public List<Location> searchByAddress(String keyword) {

        return locations.stream()
                .filter(location ->
                        location.getAddress()
                                .toLowerCase()
                                .contains(
                                        keyword.toLowerCase()))
                .toList();
    }

    public Location findById(Long id) {

        return locations.stream()
                .filter(location ->
                        location.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Sorting

    public List<Location> sortByName() {

        return locations.stream()
                .sorted(
                        Comparator.comparing(
                                Location::getName
                        )
                )
                .toList();
    }

    public List<Location> sortByCapacityDescending() {

        return locations.stream()
                .sorted(
                        Comparator.comparing(
                                Location::getCapacity
                        ).reversed()
                )
                .toList();
    }

    public List<Location> sortByOccupancyDescending() {

        return locations.stream()
                .sorted(
                        Comparator.comparing(
                                Location::getCurrentOccupancy
                        ).reversed()
                )
                .toList();
    }

    public List<Location> sortByFreePlaces() {

        return locations.stream()
                .sorted(
                        Comparator.comparing(
                                (Location location) ->
                                        location.getCapacity()
                                        - location.getCurrentOccupancy()
                        ).reversed()
                )
                .toList();
    }
}