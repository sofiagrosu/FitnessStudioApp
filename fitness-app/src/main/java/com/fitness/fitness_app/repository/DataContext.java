package com.fitness.fitness_app.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fitness.fitness_app.model.Location;
import com.fitness.fitness_app.model.UserI;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataContext {
    private static DataContext instance;

    private List<UserI> users;
    private List<Location> locations;

    private final String USERS_FILE = "src/main/resources/users.json";
    private final String LOCATIONS_FILE = "src/main/resources/locations.json";
    private final ObjectMapper objectMapper;

    private DataContext() {
        this.users = new ArrayList<>();
        this.locations = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadData();
    }

    public static DataContext getInstance() {
        if (instance == null) {
            instance = new DataContext();
        }
        return instance;
    }

    public List<UserI> getUsers() { return users; }
    public List<Location> getLocations() { return locations; }

    public void saveAllData() {
        try {
            objectMapper.writeValue(new File(USERS_FILE), users);
            objectMapper.writeValue(new File(LOCATIONS_FILE), locations);
            System.out.println("[Baza de date JSON] Datele au fost salvate cu succes!");
        } catch (IOException e) {
            System.err.println("Eroare la salvarea JSON: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            File uf = new File(USERS_FILE);
            File lf = new File(LOCATIONS_FILE);

            if (uf.getParentFile() != null) {
                uf.getParentFile().mkdirs();
            }

            if (uf.exists() && uf.length() > 0) {
                users = objectMapper.readValue(uf, new TypeReference<List<UserI>>() {});
            }
            if (lf.exists() && lf.length() > 0) {
                locations = objectMapper.readValue(lf, new TypeReference<List<Location>>() {});
            }
        } catch (IOException e) {
            System.out.println("Fișierele JSON nu au putut fi încărcate. Se inițializează liste goale.");
        }
    }
}