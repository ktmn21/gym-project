package com.example.gymcrm.dao;

import com.example.gymcrm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {
    List<TrainingType> findAll();
    Optional<TrainingType> findByName(String name);
}
