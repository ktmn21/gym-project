package com.example.gymcrm.service;

import com.example.gymcrm.model.Training;

import java.time.LocalDate;

public interface TrainingService {
    Training addTraining(String traineeUsername, String traineePassword, String trainerUsername,
                          String trainingName, String trainingTypeName, LocalDate trainingDate, Integer trainingDuration);
}
