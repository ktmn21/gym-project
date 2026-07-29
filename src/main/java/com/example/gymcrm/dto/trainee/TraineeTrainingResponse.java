package com.example.gymcrm.dto.trainee;

import java.time.LocalDate;

public class TraineeTrainingResponse {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String trainerName;

    public TraineeTrainingResponse(String trainingName, LocalDate trainingDate,
                                   String trainingType, Integer trainingDuration, String trainerName) {
        this.trainingName = trainingName; this.trainingDate = trainingDate;
        this.trainingType = trainingType; this.trainingDuration = trainingDuration;
        this.trainerName = trainerName;
    }
    public String getTrainingName() { return trainingName; }
    public LocalDate getTrainingDate() { return trainingDate; }
    public String getTrainingType() { return trainingType; }
    public Integer getTrainingDuration() { return trainingDuration; }
    public String getTrainerName() { return trainerName; }
}