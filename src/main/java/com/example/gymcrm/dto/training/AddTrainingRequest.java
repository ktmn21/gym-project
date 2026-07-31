package com.example.gymcrm.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class AddTrainingRequest {
    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Training name is required")
    private String trainingName;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    private Integer trainingDuration;

    // getters/setters
    public String getTraineeUsername() { return traineeUsername; }
    public void setTraineeUsername(String v) { this.traineeUsername = v; }
    public String getTrainerUsername() { return trainerUsername; }
    public void setTrainerUsername(String v) { this.trainerUsername = v; }
    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String v) { this.trainingName = v; }
    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate v) { this.trainingDate = v; }
    public Integer getTrainingDuration() { return trainingDuration; }
    public void setTrainingDuration(Integer v) { this.trainingDuration = v; }
}