package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.dao.TrainingTypeDao;
import com.example.gymcrm.dao.UserDao;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.model.*;
import com.example.gymcrm.service.AuthenticationService;
import com.example.gymcrm.service.TraineeService;
import com.example.gymcrm.service.TrainerService;
import com.example.gymcrm.util.UsernamePasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UserDao userDao;
    private final UsernamePasswordGenerator generator;
    private final AuthenticationService authenticationService;

    public TrainerServiceImpl(TrainerDao trainerDao, TrainingDao trainingDao, TrainingTypeDao trainingTypeDao,
                              UserDao userDao, UsernamePasswordGenerator generator,
                              AuthenticationService authenticationService) {
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.trainingTypeDao = trainingTypeDao;
        this.userDao = userDao;
        this.generator = generator;
        this.authenticationService = authenticationService;
    }

    @Override
    @Transactional
    public Trainer createProfile(String firstName, String lastName, Long specializationId) {
        validateRequired(firstName, "firstName");
        validateRequired(lastName, "lastName");
        if (specializationId == null) {
            throw new ValidationException("specializationId is required");
        }

        String username = generator.generateUsername(firstName, lastName, userDao::existsByUsername);
        String password = generator.generatePassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);

        TrainingType specialization = trainingTypeDao.findAll().stream()
                .filter(t -> t.getId().equals(specializationId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found id=" + specializationId));

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        trainerDao.save(trainer);
        log.info("Created trainer profile username={}", username);
        return trainer;
    }

    @Override
    @Transactional
    public Trainer selectByUsername(String username, String password) {
        authenticationService.authenticate(username, password);
        return findOrThrow(username);
    }

    @Override
    @Transactional
    public Trainer updateProfile(String username, String password, String firstName, String lastName, Long specializationId) {
        authenticationService.authenticate(username, password);
        validateRequired(firstName, "firstName");
        validateRequired(lastName, "lastName");

        Trainer trainer = findOrThrow(username);
        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);

        if(specializationId != null){
            TrainingType specialization = trainingTypeDao.findAll().stream()
                    .filter(t -> t.getId().equals(specializationId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("TrainingType not found id=" + specializationId));
            trainer.setSpecialization(specialization);
        }

        Trainer updated = trainerDao.update(trainer);
        log.info("Updated trainer profile username={}", username);
        return updated;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticationService.authenticate(username, oldPassword);
        validateRequired(newPassword, "newPassword");

        Trainer trainer = findOrThrow(username);
        trainer.getUser().setPassword(newPassword);
        trainerDao.update(trainer);
        log.info("Changed password for trainer username={}", username);
    }

    @Override
    @Transactional
    public void toggleActive(String username, String password) {
        authenticationService.authenticate(username, password);
        Trainer trainer = findOrThrow(username);

        boolean newState = !trainer.getUser().isActive();
        trainer.getUser().setActive(newState);

        trainerDao.update(trainer);
        log.info("Toggled active to {} for trainer username={}", newState, username);
    }

    @Override
    public List<Training> getTrainerTrainings(String username, String password, LocalDate fromDate, LocalDate toDate, String traineeName) {
        authenticationService.authenticate(username, password);
        return trainingDao.findTrainerTrainings(username, fromDate, toDate, traineeName);
    }

    @Override
    @Transactional
    public void setActiveStatus(String username, String password, boolean isActive) {
        authenticationService.authenticate(username, password);
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found username=" + username));
        trainer.getUser().setActive(isActive);
        trainerDao.update(trainer);
        log.info("Set active={} for trainer username={}", isActive, username);
    }

    private Trainer findOrThrow(String username) {
        return trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found username=" + username));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
