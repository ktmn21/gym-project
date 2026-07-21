package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {

    Trainee save(Trainee trainee);
    Optional<Trainee> findByUserName(String username);
    Trainee update (Trainee trainee);
    void delete(Trainee trainee);
    List<Trainee> findAll();

}
