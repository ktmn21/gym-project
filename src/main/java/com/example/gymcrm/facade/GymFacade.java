package com.example.gymcrm.facade;

import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.service.TraineeService;
import com.example.gymcrm.service.TrainerService;
import com.example.gymcrm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class GymFacade {

    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(String firstName, String lastName, LocalDate dob, String address) {
        log.debug("Facade: createTrainee firstName={} lastName={}", firstName, lastName);
        return traineeService.createProfile(firstName, lastName, dob, address);
    }

    public Trainee getTrainee(String username, String password) {
        return traineeService.selectByUsername(username, password);
    }

    public Trainee updateTrainee(String username, String password, String firstName,
                                 String lastName, LocalDate dob, String address) {
        return traineeService.updateProfile(username, password, firstName, lastName, dob, address);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public void toggleTraineeActive(String username, String password) {
        traineeService.toggleActive(username, password);
    }

    public void deleteTrainee(String username, String password) {
        traineeService.deleteByUsername(username, password);
    }

    public List<Training> getTraineeTrainings(String username, String password, LocalDate from,
                                              LocalDate to, String trainerName, String trainingTypeName) {
        return traineeService.getTraineeTrainings(username, password, from, to, trainerName, trainingTypeName);
    }

    public List<Trainer> getTrainersNotAssigned(String username, String password) {
        return traineeService.getTrainersNotAssigned(username, password);
    }

    public Trainee updateTraineeTrainers(String username, String password, Set<Long> trainerIds) {
        return traineeService.updateTrainersList(username, password, trainerIds);
    }

    public Trainer createTrainer(String firstName, String lastName, Long specializationId) {
        log.debug("Facade: createTrainer firstName={} lastName={}", firstName, lastName);
        return trainerService.createProfile(firstName, lastName, specializationId);
    }

    public Trainer getTrainer(String username, String password) {
        return trainerService.selectByUsername(username, password);
    }

    public Trainer updateTrainer(String username, String password, String firstName,
                                 String lastName, Long specializationId) {
        return trainerService.updateProfile(username, password, firstName, lastName, specializationId);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public void toggleTrainerActive(String username, String password) {
        trainerService.toggleActive(username, password);
    }

    public List<Training> getTrainerTrainings(String username, String password, LocalDate from,
                                              LocalDate to, String traineeName) {
        return trainerService.getTrainerTrainings(username, password, from, to, traineeName);
    }

    public Training addTraining(String traineeUsername, String traineePassword, String trainerUsername,
                                String trainingName, String trainingTypeName,
                                LocalDate trainingDate, Integer trainingDuration) {
        log.debug("Facade: addTraining name={} trainee={} trainer={}",
                trainingName, traineeUsername, trainerUsername);
        return trainingService.addTraining(traineeUsername, traineePassword, trainerUsername,
                trainingName, trainingTypeName, trainingDate, trainingDuration);
    }
}