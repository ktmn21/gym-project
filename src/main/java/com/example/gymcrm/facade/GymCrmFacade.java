//package com.example.gymcrm.facade;
//
//import com.example.gymcrm.model.Trainee;
//import com.example.gymcrm.model.Trainer;
//import com.example.gymcrm.model.Training;
//import com.example.gymcrm.service.TraineeService;
//import com.example.gymcrm.service.TrainerService;
//import com.example.gymcrm.service.TrainingService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Optional;
//
//@Component
//public class GymCrmFacade {
//
//    private static final Logger log = LoggerFactory.getLogger(GymCrmFacade.class);
//
//    private final TraineeService traineeService;
//    private final TrainerService trainerService;
//    private final TrainingService trainingService;
//
//    public GymCrmFacade(TraineeService traineeService,
//                        TrainerService trainerService,
//                        TrainingService trainingService) {
//        this.traineeService = traineeService;
//        this.trainerService = trainerService;
//        this.trainingService = trainingService;
//        log.info("GymCrmFacade initialized with all services.");
//    }
//
//    public Trainee createTrainee(Trainee trainee) {
//        log.debug("Facade: createTrainee called");
//        return traineeService.createTrainee(trainee);
//    }
//
//    public Trainee updateTrainee(Trainee trainee) {
//        log.debug("Facade: updateTrainee id={}", trainee.getUserId());
//        return traineeService.updateTrainee(trainee);
//    }
//
//    public void deleteTrainee(long id) {
//        log.debug("Facade: deleteTrainee id={}", id);
//        traineeService.deleteTrainee(id);
//    }
//
//    public Optional<Trainee> selectTrainee(long id) {
//        log.debug("Facade: selectTrainee id={}", id);
//        return traineeService.selectTrainee(id);
//    }
//
//    public List<Trainee> selectAllTrainees() {
//        log.debug("Facade: selectAllTrainees");
//        return traineeService.selectAllTrainees();
//    }
//
//    public Trainer createTrainer(Trainer trainer) {
//        log.debug("Facade: createTrainer called");
//        return trainerService.createTrainer(trainer);
//    }
//
//    public Trainer updateTrainer(Trainer trainer) {
//        log.debug("Facade: updateTrainer id={}", trainer.getUserId());
//        return trainerService.updateTrainer(trainer);
//    }
//
//    public Optional<Trainer> selectTrainer(long id) {
//        log.debug("Facade: selectTrainer id={}", id);
//        return trainerService.selectTrainer(id);
//    }
//
//    public List<Trainer> selectAllTrainers() {
//        log.debug("Facade: selectAllTrainers");
//        return trainerService.selectAllTrainers();
//    }
//
//    public Training createTraining(Training training) {
//        log.debug("Facade: createTraining called");
//        return trainingService.createTraining(training);
//    }
//
//    public Optional<Training> selectTraining(long id) {
//        log.debug("Facade: selectTraining id={}", id);
//        return trainingService.selectTraining(id);
//    }
//
//    public List<Training> selectAllTrainings() {
//        log.debug("Facade: selectAllTrainings");
//        return trainingService.selectAllTrainings();
//    }
//}
