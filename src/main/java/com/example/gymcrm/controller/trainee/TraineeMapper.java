package com.example.gymcrm.controller.trainee;

import com.example.gymcrm.dto.trainee.*;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import java.util.List;
import java.util.stream.Collectors;

public final class TraineeMapper {
    private TraineeMapper() {}

    public static TraineeProfileResponse toProfileResponse(Trainee trainee) {
        List<TrainerSummary> trainers = trainee.getTrainers().stream()
                .map(TraineeMapper::toTrainerSummary).collect(Collectors.toList());
        return new TraineeProfileResponse(
                trainee.getUser().getUsername(), trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(), trainee.getDateOfBirth(),
                trainee.getAddress(), trainee.getUser().isActive(), trainers);
    }

    public static TrainerSummary toTrainerSummary(Trainer trainer) {
        return new TrainerSummary(
                trainer.getUser().getUsername(), trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(), trainer.getSpecialization().getTrainingTypeName());
    }

    public static TraineeTrainingResponse toTrainingResponse(Training training) {
        return new TraineeTrainingResponse(
                training.getTrainingName(), training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                training.getTrainer().getUser().getUsername());
    }
}