package com.fitness.fitness_app.repository;

import java.util.List;

public interface FileRepository<T> {
 // un repository care sa citeasca din fisiere json 
 List<T> getAll();
 void SaveAll(List<T> items);
 void add(T item);
 void update(T item);
 void delete(Long id);
 public T findById(Long id);
 public List<String> getAllInformation();

}
