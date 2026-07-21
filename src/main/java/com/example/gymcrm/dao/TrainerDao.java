package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer save(Trainer trainer);
    Optional<Trainer> findByUsername(String username);
    Trainer update(Trainer trainer);
    List<Trainer> findAllNotAssignedToTrainee(String traineeUsername);
}
