package com.example.gymcrm.service;

import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    @Autowired
    private TrainingDao trainingDao;

    public Optional<Training> selectTraining(long id){
        log.info("Selecting training with id = {}", id);
        Optional<Training> result = trainingDao.findById(id);
        if(result.isEmpty()){
            log.warn("training with Id = {} not found", id);
        }
        return result;
    }

    public Training createTraining (Training training){
        log.info("Creating training '{}' for traineeId={}, trainerId={}",
                training.getTrainingName(), training.getTraineeId(), training.getTrainerId());

        Training saved = trainingDao.save(training);
        log.info("Training created: id={}, name={}", saved.getId(), saved.getTrainingName());
        return saved;
    }

    public List<Training> selectAllTrainings(){
        log.info("Selecting all Trainings");
        return trainingDao.findAll();
    }

}
