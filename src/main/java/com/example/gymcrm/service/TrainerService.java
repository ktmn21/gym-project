package com.example.gymcrm.service;

import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);
    private UsernamePasswordGenerator usernamePasswordGenerator;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    public void setUsernamePasswordGenerator(UsernamePasswordGenerator usernamePasswordGenerator) {
        this.usernamePasswordGenerator = usernamePasswordGenerator;
    }

    public Trainer createTrainer(Trainer trainer) {
        log.info("Creating trainer profile for {}.{}", trainer.getFirstName(), trainer.getLastName());
        String username = usernamePasswordGenerator.generateUserName(trainer.getFirstName(), trainer.getLastName());
        String password = usernamePasswordGenerator.generatePassword();
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setActive(true);
        Trainer saved = trainerDao.save(trainer);
        log.info("Trainer created successfully: id={}, username={}", saved.getUserId(), saved.getUsername());
        return saved;
    }

    public Trainer updateTrainer(Trainer trainer) {
        log.info("Updating trainer id={}", trainer.getUserId());
        if (trainerDao.findById(trainer.getUserId()).isEmpty()) {
            log.warn("Update failed — trainer id={} not found", trainer.getUserId());
            throw new IllegalArgumentException("Trainer not found: id=" + trainer.getUserId());
        }
        Trainer updated = trainerDao.update(trainer);
        log.info("Trainer updated: id={}", updated.getUserId());
        return updated;
    }

    public Optional<Trainer> selectTrainer(long id) {
        log.info("Selecting trainer id={}", id);
        Optional<Trainer> result = trainerDao.findById(id);
        if (result.isEmpty()) {
            log.warn("Trainer id={} not found", id);
        }
        return result;
    }

    public Optional<Trainer> selectTrainerByUsername(String username) {
        log.info("Selecting trainer by username={}", username);
        return trainerDao.findByUsername(username);
    }

    public List<Trainer> selectAllTrainers() {
        log.info("Selecting all trainers");
        return trainerDao.findAll();
    }
}
