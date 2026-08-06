package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeRepository;
import com.example.gymcrm.dao.TrainerRepository;
import com.example.gymcrm.dao.TrainingRepository;
import com.example.gymcrm.dao.TrainingTypeRepository;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.service.AuthenticationService;
import com.example.gymcrm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthenticationService authenticationService;

    public TrainingServiceImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository, TrainingRepository trainingRepository,
                               TrainingTypeRepository trainingTypeRepository, AuthenticationService authenticationService) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.authenticationService = authenticationService;
    }

    @Override
    @Transactional
    public Training addTraining(String traineeUsername, String traineePassword, String trainerUsername, String trainingName, String trainingTypeName, LocalDate trainingDate, Integer trainingDuration) {
        authenticationService.authenticate(traineeUsername, traineePassword);

        require(trainingName, "trainingName");
        require(trainingTypeName, "trainingTypeName");
        if (trainingDate == null) throw new ValidationException("trainingDate is required");
        if (trainingDuration == null) throw new ValidationException("trainingDuration is required");


        Trainee trainee = traineeRepository.findByUserName(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found username=" + traineeUsername));
        Trainer trainer = trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found username=" + trainerUsername));
        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(trainingTypeName)
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found name=" + trainingTypeName));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(trainingDuration);

        trainingRepository.save(training);
        log.info("Added training '{}' for trainee={} trainer={}", trainingName, traineeUsername, trainerUsername);
        return training;
    }

    private void require(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
