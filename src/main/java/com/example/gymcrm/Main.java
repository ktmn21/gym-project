package com.example.gymcrm;

import com.example.gymcrm.config.AppConfig;
import com.example.gymcrm.facade.GymFacade;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade facade = context.getBean(GymFacade.class);

            log.info("\n--- Create Trainer ---");
            Trainer trainer = facade.createTrainer("Jane", "Smith", 4L);
            String trainerUsername = trainer.getUser().getUsername();
            String trainerPassword = trainer.getUser().getPassword();
            log.info("Created trainer -> username={}, password={}", trainerUsername, trainerPassword);

            log.info("\n--- Create Trainee ---");
            Trainee trainee = facade.createTrainee("John", "Doe",
                    LocalDate.of(1995, 3, 15), "123 Main St");
            String traineeUsername = trainee.getUser().getUsername();
            String traineePassword = trainee.getUser().getPassword();
            log.info("Created trainee -> username={}, password={}", traineeUsername, traineePassword);

            log.info("\n--- Select profiles by username ---");
            Trainer fetchedTrainer = facade.getTrainer(trainerUsername, trainerPassword);
            log.info("Fetched trainer: {} {}", fetchedTrainer.getUser().getFirstName(),
                    fetchedTrainer.getUser().getLastName());
            Trainee fetchedTrainee = facade.getTrainee(traineeUsername, traineePassword);
            log.info("Fetched trainee: {} {}", fetchedTrainee.getUser().getFirstName(),
                    fetchedTrainee.getUser().getLastName());

            log.info("\n--- Change passwords ---");
            facade.changeTraineePassword(traineeUsername, traineePassword, "newTraineePass1");
            traineePassword = "newTraineePass1";
            log.info("Trainee password changed.");

            facade.changeTrainerPassword(trainerUsername, trainerPassword, "newTrainerPass1");
            trainerPassword = "newTrainerPass1";
            log.info("Trainer password changed.");

            log.info("\n--- Update profiles ---");
            facade.updateTrainer(trainerUsername, trainerPassword, "Jane", "Smith-Updated", 4L);
            log.info("Trainer profile updated.");

            facade.updateTrainee(traineeUsername, traineePassword, "John", "Doe-Updated",
                    LocalDate.of(1995, 3, 15), "456 New Ave");
            log.info("Trainee profile updated.");

            log.info("\n--- Toggle active (non-idempotent) ---");
            facade.toggleTraineeActive(traineeUsername, traineePassword);
            log.info("Trainee active now: {}",
                    facade.getTrainee(traineeUsername, traineePassword).getUser().isActive());
            facade.toggleTraineeActive(traineeUsername, traineePassword);   // toggle back
            log.info("Trainee active now: {}",
                    facade.getTrainee(traineeUsername, traineePassword).getUser().isActive());

            facade.toggleTrainerActive(trainerUsername, trainerPassword);
            log.info("Trainer active now: {}",
                    facade.getTrainer(trainerUsername, trainerPassword).getUser().isActive());
            facade.toggleTrainerActive(trainerUsername, trainerPassword);   // toggle back

            log.info("\n--- Add training ---");
            Training training = facade.addTraining(
                    traineeUsername, traineePassword, trainerUsername,
                    "Morning Cardio", "Cardio",
                    LocalDate.of(2024, 6, 1), 60);
            log.info("Added training: {} on {}", training.getTrainingName(), training.getTrainingDate());

            log.info("\n--- Trainee trainings by criteria ---");
            List<Training> traineeTrainings = facade.getTraineeTrainings(
                    traineeUsername, traineePassword,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                    null, "Cardio");
            log.info("Trainee has {} trainings matching criteria.", traineeTrainings.size());

            log.info("\n--- Trainer trainings by criteria ---");
            List<Training> trainerTrainings = facade.getTrainerTrainings(
                    trainerUsername, trainerPassword,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                    null);
            log.info("Trainer has {} trainings matching criteria.", trainerTrainings.size());

            log.info("\n--- Trainers not assigned to trainee ---");
            List<Trainer> notAssigned = facade.getTrainersNotAssigned(traineeUsername, traineePassword);
            log.info("Unassigned trainers: {}", notAssigned.stream()
                    .map(t -> t.getUser().getUsername())
                    .collect(Collectors.toList()));


            log.info("\n--- Update trainee's trainers list ---");
            if (!notAssigned.isEmpty()) {
                Long trainerId = notAssigned.get(0).getId();
                Trainee updated = facade.updateTraineeTrainers(
                        traineeUsername, traineePassword, Set.of(trainerId));
                log.info("Trainee now has {} trainer(s) assigned.", updated.getTrainers().size());
            } else {
                log.info("No unassigned trainers available to assign.");
            }

            log.info("\n--- Delete trainee  ---");
            facade.deleteTrainee(traineeUsername, traineePassword);
            log.info("Trainee deleted.");


        } catch (Exception e) {
            log.error("Demonstration failed", e);
        }
    }
}