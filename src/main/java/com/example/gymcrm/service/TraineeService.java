package com.example.gymcrm.service;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);
    private UsernamePasswordGenerator usernamePasswordGenerator;

    @Autowired
    public void setUsernamePasswordGenerator(UsernamePasswordGenerator usernamePasswordGenerator) {
        this.usernamePasswordGenerator = usernamePasswordGenerator;
    }

    @Autowired
    private TraineeDao traineeDao;

    public Trainee createTrainee(Trainee trainee){

        log.info("Creating trainee profile for {}.{}", trainee.getFirstName(), trainee.getLastName());
        String username = usernamePasswordGenerator.generateUserName(trainee.getFirstName(), trainee.getLastName());
        String password = usernamePasswordGenerator.generatePassword();
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setActive(true);
        Trainee saved = traineeDao.save(trainee);
        log.info("Trainee created successfully: id={}, username={}", saved.getUserId(), saved.getUsername());
        return saved;
    }

    public Trainee updateTrainee(Trainee trainee){
        log.info("Updating trainee with id: {}" , trainee.getUserId());
        if(traineeDao.findById(trainee.getUserId()).isEmpty()){
            log.warn("Update failed — trainee id={} not found", trainee.getUserId());
            throw new IllegalArgumentException("Trainee not found");
        }
        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee successfully update id: {}",trainee.getUserId());
        return updated;
    }

    public void deleteTrainee(long id) {
        log.info("Deleting trainee id={}", id);
        if (traineeDao.findById(id).isEmpty()) {
            log.warn("Delete failed — trainee id={} not found", id);
            throw new IllegalArgumentException("Trainee not found: id=" + id);
        }
        traineeDao.deleteById(id);
        log.info("Trainee id={} deleted", id);
    }

    public Optional<Trainee> selectTrainee(long id) {
        log.info("Selecting trainee id={}", id);
        Optional<Trainee> result = traineeDao.findById(id);
        if (result.isEmpty()) {
            log.warn("Trainee id={} not found", id);
        }
        return result;
    }

    public Optional<Trainee> selectTraineeByUsername(String username) {
        log.info("Selecting trainee by username={}", username);
        return traineeDao.findByUsername(username);
    }

    public List<Trainee> selectAllTrainees() {
        log.info("Selecting all trainees");
        return traineeDao.findAll();
    }
}
