package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeRepository;
import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.dao.UserDao;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.User;
import com.example.gymcrm.service.AuthenticationService;
import com.example.gymcrm.service.TraineeService;
import com.example.gymcrm.util.UsernamePasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeRepository traineeRepository;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final UserDao userDao;
    private final UsernamePasswordGenerator generator;
    private final AuthenticationService authenticationService;

    public TraineeServiceImpl(TraineeRepository traineeRepository, TrainerDao trainerDao, TrainingDao trainingDao,
                              UserDao userDao, UsernamePasswordGenerator generator,
                              AuthenticationService authenticationService) {
        this.traineeRepository = traineeRepository;
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.userDao = userDao;
        this.generator = generator;
        this.authenticationService = authenticationService;
    }

    @Override
    @Transactional
    public Trainee createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        validateRequired(firstName, "firstName");
        validateRequired(lastName, "lastName");

        String username = generator.generateUsername(firstName, lastName, userDao::existsByUsername);
        String password = generator.generatePassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        traineeRepository.save(trainee);
        log.info("Created trainee profile username={}", username);
        return trainee;
    }

    @Override
    public Trainee selectByUsername(String username, String password) {
        authenticationService.authenticate(username, password);
        return findOrThrow(username);
    }

    @Override
    @Transactional
    public Trainee updateProfile(String username, String password, String firstName, String lastName, LocalDate dateOfBirth, String address) {
        authenticationService.authenticate(username, password);
        validateRequired(firstName, "firstName");
        validateRequired(lastName, "lastName");

        Trainee trainee = findOrThrow(username);
        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        Trainee updated = traineeRepository.save(trainee);
        log.info("Updated trainee profile username={}", username);
        return updated;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticationService.authenticate(username, oldPassword);
        validateRequired(newPassword, "newPassword");

        Trainee trainee = findOrThrow(username);
        trainee.getUser().setPassword(newPassword);
        traineeRepository.save(trainee);
        log.info("Changed password for trainee username={}", username);
    }

    @Override
    @Transactional
    public void toggleActive(String username, String password) {
        authenticationService.authenticate(username, password);
        Trainee trainee = findOrThrow(username);

        boolean newState = !trainee.getUser().isActive();   // ← FLIP the current value
        trainee.getUser().setActive(newState);

        traineeRepository.save(trainee);
        log.info("Toggled active to {} for trainee username={}", newState, username);
    }

    @Override
    @Transactional
    public void deleteByUsername(String username, String password) {
        authenticationService.authenticate(username, password);
        Trainee trainee = findOrThrow(username);
        traineeRepository.delete(trainee);
        log.info("Deleted trainee username={} (cascade removed trainings)", username);
    }

    @Override
    public List<Training> getTraineeTrainings(String username, String password, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        authenticationService.authenticate(username, password);
        return trainingDao.findTraineeTrainings(username, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Override
    public List<Trainer> getTrainersNotAssigned(String username, String password) {

        authenticationService.authenticate(username, password);
        return trainerDao.findAllNotAssignedToTrainee(username);

    }

    @Override
    @Transactional
    public Trainee updateTrainersList(String username, String password, Set<Long> trainerIds) {
        authenticationService.authenticate(username, password);
        Trainee trainee = findOrThrow(username);

        Set<Trainer> currentTrainers = new HashSet<>(trainee.getTrainers());
        Set<Trainer> availableTrainers = new HashSet<>(trainerDao.findAllNotAssignedToTrainee(username));

        Map<Long, Trainer> allowedById = new HashMap<>();
        for (Trainer trainer : currentTrainers) {
            allowedById.put(trainer.getId(), trainer);
        }
        for (Trainer trainer : availableTrainers) {
            allowedById.put(trainer.getId(), trainer);
        }

        Set<Trainer> newTrainers = new HashSet<>();
        for (Long id : trainerIds) {
            Trainer trainer = allowedById.get(id);
            if (trainer == null) {
                throw new EntityNotFoundException("Trainer not found id=" + id);
            }
            newTrainers.add(trainer);
        }

        for (Trainer oldTrainer : new HashSet<>(trainee.getTrainers())) {
            oldTrainer.getTrainees().remove(trainee);
        }
        trainee.getTrainers().clear();

        for (Trainer trainer : newTrainers) {
            trainee.getTrainers().add(trainer);
            trainer.getTrainees().add(trainee);
        }

        Trainee updated = traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee username={}, count={}", username, newTrainers.size());
        return updated;
    }

    @Override
    @Transactional
    public void setActiveStatus(String username, String password, boolean isActive) {
        authenticationService.authenticate(username, password);
        Trainee trainee = findOrThrow(username);
        trainee.getUser().setActive(isActive);
        traineeRepository.save(trainee);
        log.info("Set active={} for trainee username={}", isActive, username);
    }

    @Override
    @Transactional
    public Trainee updateTrainersListByUsername(String username, String password, Set<String> trainerUsernames) {
        authenticationService.authenticate(username, password);
        Trainee trainee = findOrThrow(username);

        Set<Trainer> newTrainers = new HashSet<>();
        for (String trainerUsername : trainerUsernames) {
            Trainer trainer = trainerDao.findByUsername(trainerUsername)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found username=" + trainerUsername));
            newTrainers.add(trainer);
        }

        for (Trainer oldTrainer : new HashSet<>(trainee.getTrainers())) {
            oldTrainer.getTrainees().remove(trainee);
        }
        trainee.getTrainers().clear();

        for (Trainer trainer : newTrainers) {
            trainee.getTrainers().add(trainer);
            trainer.getTrainees().add(trainee);
        }

        Trainee updated = traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee username={}, count={}", username, newTrainers.size());
        return updated;
    }

    private Trainee findOrThrow(String username) {
        return traineeRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found username=" + username));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
