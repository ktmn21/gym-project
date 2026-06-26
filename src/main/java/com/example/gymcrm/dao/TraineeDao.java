package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {

    Trainee save(Trainee trainee);
    Trainee update(Trainee trainee);
    void deleteById(long id);
    Optional<Trainee> findById(Long id);
    Optional<Trainee> findByUsername(String username);
    List<Trainee> findAll();

}
