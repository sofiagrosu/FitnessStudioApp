package com.fitness.fitness_app.repository;

import java.util.List;

public interface BaseRepository<T> {
    T save(T entity);
    T findById(Long id);
    List<T> findAll();

    default void deleteById(Long id) {
        throw new UnsupportedOperationException("Delete not supported for this entity");
    }
}
