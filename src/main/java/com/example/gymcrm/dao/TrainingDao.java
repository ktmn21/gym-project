package com.example.gymcrm.dao;

import com.example.gymcrm.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    void save(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
}
