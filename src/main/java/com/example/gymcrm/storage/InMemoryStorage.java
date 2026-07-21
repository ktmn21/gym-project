package com.example.gymcrm.storage;

import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryStorage {

    private final Logger log = LoggerFactory.getLogger(InMemoryStorage.class);

    @Value("${storage.seed.trainers}")
    private String trainersSeedPath;

    @Value("${storage.seed.trainees}")
    private String traineesSeedPath;

    @Value("${storage.seed.trainings}")
    private String trainingsSeedPath;


    private final Map<Long, Trainee> traineeStorage = new HashMap<>();
    private final Map<Long, Trainer> trainerStorage = new HashMap<>();
    private final Map<Long, Training> trainingStorage = new HashMap<>();

    public Map<Long, Trainee> getTraineeStorage() {
        return traineeStorage;
    }

    public Map<Long, Trainer> getTrainerStorage() {
        return trainerStorage;
    }

    public Map<Long, Training> getTrainingStorage() {
        return trainingStorage;
    }


}