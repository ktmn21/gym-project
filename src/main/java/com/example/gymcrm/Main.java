package com.example.gymcrm;

import com.example.gymcrm.config.AppConfig;
import com.example.gymcrm.facade.GymCrmFacade;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application...");

        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymCrmFacade facade = context.getBean(GymCrmFacade.class);

            Trainer trainer = new Trainer("John", "Smith", TrainingType.CARDIO);
            trainer = facade.createTrainer(trainer);
            log.info("Created: {}", trainer);

            Trainee trainee = new Trainee("Jane", "Doe", LocalDate.of(1995, 6, 15), "123 Main St");
            trainee = facade.createTrainee(trainee);
            log.info("Created: {}", trainee);

            Training training = new Training(
                    trainee.getUserId(), trainer.getUserId(),
                    "Morning Cardio Session", TrainingType.CARDIO,
                    LocalDate.now(), 60
            );
            training = facade.createTraining(training);
            log.info("Created: {}", training);

            facade.selectTrainer(trainer.getUserId()).ifPresent(t -> log.info("Found: {}", t));
            log.info("Total trainees: {}", facade.selectAllTrainees().size());
            log.info("Total trainings: {}", facade.selectAllTrainings().size());

            facade.deleteTrainee(trainee.getUserId());
            log.info("Trainee deleted. Remaining trainees: {}", facade.selectAllTrainees().size());
        }

        log.info("Gym CRM Application finished.");
    }
}
