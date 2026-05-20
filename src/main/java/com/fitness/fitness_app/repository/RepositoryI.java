package com.fitness.fitness_app.repository;

import java.util.List;

public interface RepositoryI<T> {
    void save(T entity);
    T findById(Long id);
    List<T> findAll();
    void deleteById(Long id);
}