package com.example.gymcrm.dto.trainingtype;

public class TrainingTypeResponse {
    private Long trainingTypeId;
    private String trainingType;

    public TrainingTypeResponse(Long trainingTypeId, String trainingType) {
        this.trainingTypeId = trainingTypeId;
        this.trainingType = trainingType;
    }
    public Long getTrainingTypeId() { return trainingTypeId; }
    public String getTrainingType() { return trainingType; }
}