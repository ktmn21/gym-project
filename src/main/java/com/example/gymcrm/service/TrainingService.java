package com.example.gymcrm.service;

import com.example.gymcrm.model.Training;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface TrainingService {
    Training addTraining(String traineeUsername, String trainerUsername,
                          String trainingName, String trainingTypeName, LocalDate trainingDate, Integer trainingDuration);

    void deleteTraining(Long trainingId);
}
