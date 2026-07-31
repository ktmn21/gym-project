package com.example.gymcrm.controller.trainer;

import com.example.gymcrm.dto.trainer.*;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import java.util.List;
import java.util.stream.Collectors;

public final class TrainerMapper {
    private TrainerMapper() {}

    public static TrainerProfileResponse toProfileResponse(Trainer trainer) {
        List<TraineeSummary> trainees = trainer.getTrainees().stream()
                .map(TrainerMapper::toTraineeSummary).collect(Collectors.toList());
        return new TrainerProfileResponse(
                trainer.getUser().getUsername(), trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().isActive(), trainees);
    }

    public static TraineeSummary toTraineeSummary(Trainee trainee) {
        return new TraineeSummary(
                trainee.getUser().getUsername(), trainee.getUser().getFirstName(),
                trainee.getUser().getLastName());
    }

    public static TrainerTrainingResponse toTrainingResponse(Training training) {
        return new TrainerTrainingResponse(
                training.getTrainingName(), training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                training.getTrainee().getUser().getUsername());
    }
}