package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.dao.TrainingTypeDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;

    public TrainingServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao, TrainingDao trainingDao,
                               TrainingTypeDao trainingTypeDao, AuthenticationService authenticationService) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.trainingTypeDao = trainingTypeDao;
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


        Trainee trainee = traineeDao.findByUserName(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found username=" + traineeUsername));
        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found username=" + trainerUsername));
        TrainingType trainingType = trainingTypeDao.findByName(trainingTypeName)
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found name=" + trainingTypeName));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(trainingDuration);

        trainingDao.save(training);
        log.info("Added training '{}' for trainee={} trainer={}", trainingName, traineeUsername, trainerUsername);
        return training;
    }

    private void require(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
