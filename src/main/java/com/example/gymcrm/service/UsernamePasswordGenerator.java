package com.example.gymcrm.service;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.dao.TrainerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class UsernamePasswordGenerator {

    private static final Logger log = LoggerFactory.getLogger(UsernamePasswordGenerator.class);
    private static final SecureRandom rand = new SecureRandom();
    private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    private TrainerDao trainerDao;
    private TraineeDao traineeDao;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    public String generateUserName(String firstName, String lastName){
        String base = firstName + "." + lastName;
        String candidate = base;
        int serialNumber = 1;

        while(!isUsernameFree(candidate)){
            candidate = base + "." + serialNumber;
            serialNumber ++;
        }

        log.debug("Generated userName: {} for the {} {}", candidate, firstName, lastName);
        return candidate;
    }

    public String generatePassword(){;
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i ++){
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private boolean isUsernameFree(String userName){
        return traineeDao.findByUsername(userName).isEmpty() && trainerDao.findByUsername(userName).isEmpty();
    }

}
