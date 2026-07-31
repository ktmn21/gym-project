package com.example.gymcrm.dto.trainer;

import java.time.LocalDate;

public class TrainerTrainingResponse {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String traineeName;

    public TrainerTrainingResponse(String trainingName, LocalDate trainingDate,
                                   String trainingType, Integer trainingDuration, String traineeName) {
        this.trainingName = trainingName; this.trainingDate = trainingDate;
        this.trainingType = trainingType; this.trainingDuration = trainingDuration;
        this.traineeName = traineeName;
    }
    public String getTrainingName() { return trainingName; }
    public LocalDate getTrainingDate() { return trainingDate; }
    public String getTrainingType() { return trainingType; }
    public Integer getTrainingDuration() { return trainingDuration; }
    public String getTraineeName() { return traineeName; }
}