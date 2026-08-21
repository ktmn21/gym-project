package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TrainerRepository;
import com.example.gymcrm.dao.TrainingRepository;
import com.example.gymcrm.dao.TrainingTypeRepository;
import com.example.gymcrm.dao.UserRepository;
import com.example.gymcrm.exceptions.AuthenticationException;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.metrics.GymMetrics;
import com.example.gymcrm.model.*;
import com.example.gymcrm.service.TrainerService;
import com.example.gymcrm.util.UsernamePasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;
    private final UsernamePasswordGenerator generator;
    private final GymMetrics gymMetrics;
    private final PasswordEncoder passwordEncoder;

    public TrainerServiceImpl(TrainerRepository trainerRepository, TrainingRepository trainingRepository, TrainingTypeRepository trainingTypeRepository,
                              UserRepository userRepository, UsernamePasswordGenerator generator,
                              GymMetrics gymMetrics, PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userRepository = userRepository;
        this.generator = generator;
        this.gymMetrics = gymMetrics;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Trainer createProfile(String firstName, String lastName, Long specializationId) {
        validateRequired(firstName, "firstName");
        validateRequired(lastName, "lastName");
        if (specializationId == null) {
            throw new ValidationException("specializationId is required");
        }

        String username = generator.generateUsername(firstName, lastName, userRepository::existsByUsername);
        String rawPassword = generator.generatePassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.addAuthority(Role.ROLE_TRAINER);
        user.setRawPassword(rawPassword);

        TrainingType specialization = trainingTypeRepository.findAll().stream()
                .filter(t -> t.getId().equals(specializationId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found id=" + specializationId));

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        trainerRepository.save(trainer);
        gymMetrics.incrementTrainerCreated();
        log.info("Created trainer profile username={}", username);
        return trainer;
    }

    @Override
    @Transactional
    public Trainer selectByUsername(String username) {

        return findOrThrow(username);
    }

    @Override
    @Transactional
    public Trainer updateProfile(String username, String firstName, String lastName, Long specializationId) {

        validateRequired(firstName, "firstName");
        validateRequired(lastName, "lastName");

        Trainer trainer = findOrThrow(username);
        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);

        if(specializationId != null){
            TrainingType specialization = trainingTypeRepository.findAll().stream()
                    .filter(t -> t.getId().equals(specializationId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("TrainingType not found id=" + specializationId));
            trainer.setSpecialization(specialization);
        }

        Trainer updated = trainerRepository.save(trainer);
        log.info("Updated trainer profile username={}", username);
        return updated;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        validateRequired(newPassword, "newPassword");
        Trainer trainer = findOrThrow(username);

        if (!passwordEncoder.matches(oldPassword, trainer.getUser().getPassword())) {
            throw new AuthenticationException("Old password is incorrect");
        }

        trainer.getUser().setPassword(passwordEncoder.encode(newPassword));
        trainerRepository.save(trainer);
        log.info("Changed password for trainee username={}", username);
    }

    @Override
    @Transactional
    public void toggleActive(String username) {

        Trainer trainer = findOrThrow(username);

        boolean newState = !trainer.getUser().isActive();
        trainer.getUser().setActive(newState);

        trainerRepository.save(trainer);
        log.info("Toggled active to {} for trainer username={}", newState, username);
    }

    @Override
    public List<Training> getTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {

        return trainingRepository.findTrainerTrainings(username, fromDate, toDate, traineeName);
    }

    @Override
    @Transactional
    public void setActiveStatus(String username, boolean isActive) {

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found username=" + username));
        trainer.getUser().setActive(isActive);
        trainerRepository.save(trainer);
        log.info("Set active={} for trainer username={}", isActive, username);
    }

    private Trainer findOrThrow(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found username=" + username));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
