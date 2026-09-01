package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeRepository;
import com.example.gymcrm.dao.TrainerRepository;
import com.example.gymcrm.dao.TrainingRepository;
import com.example.gymcrm.dao.TrainingTypeRepository;
import com.example.gymcrm.dto.trainer.ActionType;
import com.example.gymcrm.dto.trainer.TrainerWorkloadRequest;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.service.TrainingService;
import com.example.gymcrm.service.WorkloadIntegrationService;
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
    private final WorkloadIntegrationService workloadIntegrationService;

    public TrainingServiceImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository, TrainingRepository trainingRepository,
                               TrainingTypeRepository trainingTypeRepository, WorkloadIntegrationService workloadIntegrationService) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.workloadIntegrationService = workloadIntegrationService;

    }

    @Override
    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername, String trainingName, String trainingTypeName, LocalDate trainingDate, Integer trainingDuration) {

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

        TrainerWorkloadRequest workloadRequest = new TrainerWorkloadRequest(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().isActive(),
                trainingDate,
                trainingDuration,
                ActionType.ADD
        );
        workloadIntegrationService.sendWorkload(workloadRequest);

        return training;
    }

    @Override
    @Transactional
    public void deleteTraining(Long trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Training not found id=" + trainingId));

        Trainer trainer = training.getTrainer();

        TrainerWorkloadRequest workloadRequest = new TrainerWorkloadRequest(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                ActionType.DELETE
        );

        trainingRepository.delete(training);
        log.info("Deleted training id={}", trainingId);

        workloadIntegrationService.sendWorkload(workloadRequest);
    }

    private void require(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
