package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeRepository;
import com.example.gymcrm.dao.TrainerRepository;
import com.example.gymcrm.dao.TrainingRepository;
import com.example.gymcrm.dao.UserRepository;
import com.example.gymcrm.dto.trainee.TraineeRegistrationResponse;
import com.example.gymcrm.exceptions.AuthenticationException;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.metrics.GymMetrics;
import com.example.gymcrm.model.*;
import com.example.gymcrm.service.TraineeService;
import com.example.gymcrm.util.UsernamePasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.*;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final UsernamePasswordGenerator generator;
    private final PasswordEncoder passwordEncoder;

    private final GymMetrics gymMetrics;

    public TraineeServiceImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository, TrainingRepository trainingRepository,
                              UserRepository userRepository, UsernamePasswordGenerator generator,
                               GymMetrics gymMetrics, PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.userRepository = userRepository;
        this.generator = generator;
        this.gymMetrics = gymMetrics;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public TraineeRegistrationResponse createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        validateRequired(firstName, "firstName");
        validateRequired(lastName, "lastName");

        String username = generator.generateUsername(firstName, lastName, userRepository::existsByUsername);
        String rawPassword = generator.generatePassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.addAuthority(Role.ROLE_TRAINEE);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        traineeRepository.save(trainee);
        gymMetrics.incrementTraineeCreated();
        log.info("Created trainee profile username={}", username);
        return new TraineeRegistrationResponse(username, rawPassword);
    }

    @Override
    public Trainee selectByUsername(String username) {
        return findOrThrow(username);
    }

    @Override
    @Transactional
    public Trainee updateProfile(String username, String firstName, String lastName, LocalDate dateOfBirth, String address) {
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
        validateRequired(newPassword, "newPassword");
        Trainee trainee = findOrThrow(username);

        if (!passwordEncoder.matches(oldPassword, trainee.getUser().getPassword())) {
            throw new AuthenticationException("Old password is incorrect");
        }

        trainee.getUser().setPassword(passwordEncoder.encode(newPassword));
        traineeRepository.save(trainee);
        log.info("Changed password for trainee username={}", username);
    }

    @Override
    @Transactional
    public void toggleActive(String username) {
        Trainee trainee = findOrThrow(username);

        boolean newState = !trainee.getUser().isActive();
        trainee.getUser().setActive(newState);

        traineeRepository.save(trainee);
        log.info("Toggled active to {} for trainee username={}", newState, username);
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        Trainee trainee = findOrThrow(username);
        traineeRepository.delete(trainee);
        log.info("Deleted trainee username={} (cascade removed trainings)", username);
    }

    @Override
    public List<Training> getTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        return trainingRepository.findTraineeTrainings(username, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Override
    public List<Trainer> getTrainersNotAssigned(String username) {

        return trainerRepository.findAllNotAssignedToTrainee(username);

    }

    @Override
    @Transactional
    public Trainee updateTrainersList(String username, Set<Long> trainerIds) {
        Trainee trainee = findOrThrow(username);

        Set<Trainer> currentTrainers = new HashSet<>(trainee.getTrainers());
        Set<Trainer> availableTrainers = new HashSet<>(trainerRepository.findAllNotAssignedToTrainee(username));

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
    public void setActiveStatus(String username, boolean isActive) {
        Trainee trainee = findOrThrow(username);
        trainee.getUser().setActive(isActive);
        traineeRepository.save(trainee);
        log.info("Set active={} for trainee username={}", isActive, username);
    }

    @Override
    @Transactional
    public Trainee updateTrainersListByUsername(String username, Set<String> trainerUsernames) {
        Trainee trainee = findOrThrow(username);

        Set<Trainer> newTrainers = new HashSet<>();
        for (String trainerUsername : trainerUsernames) {
            Trainer trainer = trainerRepository.findByUsername(trainerUsername)
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
