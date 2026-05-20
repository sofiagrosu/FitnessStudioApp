package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.model.Location;
import com.fitness.fitness_app.repository.LocationRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final CoursesService coursesService;

    public LocationService(LocationRepository locationRepository,
                           @Lazy CoursesService coursesService) {
        this.locationRepository = locationRepository;
        this.coursesService = coursesService;
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Long locationId) {
        if (locationId == null) {
            throw new ValidationException("Location id is required");
        }

        return locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location not found"));
    }

    public Location createLocation(Location location) {
        validateLocationData(location);
        return locationRepository.save(location);
    }

    public Location updateLocation(Long locationId, Location location) {
        Location existingLocation = getLocationById(locationId);
        validateLocationData(location);

        existingLocation.setName(location.getName());
        existingLocation.setAddress(location.getAddress());

        return locationRepository.save(existingLocation);
    }

    public void deleteLocation(Long locationId) {
        getLocationById(locationId);

        List<Course> coursesAtLocation = coursesService.getCoursesByLocation(locationId);
        for (Course course : coursesAtLocation) {
            coursesService.deleteCourse(course.getId());
        }

        locationRepository.deleteById(locationId);
    }

    private void validateLocationData(Location location) {
        if (location == null) {
            throw new ValidationException("Location data is required");
        }

        if (location.getName() == null || location.getName().isBlank()) {
            throw new ValidationException("Location name is required");
        }

        if (location.getAddress() == null || location.getAddress().isBlank()) {
            throw new ValidationException("Location address is required");
        }
    }
}