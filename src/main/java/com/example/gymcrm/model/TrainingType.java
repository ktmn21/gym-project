package com.example.gymcrm.model;

public class TrainingType {
    private Long trainingTypeId;
    private String trainingTypeName;

    public TrainingType() {
    }

    public TrainingType(String trainingTypeName, Long trainingTypeId) {
        this.trainingTypeName = trainingTypeName;
        this.trainingTypeId = trainingTypeId;
    }

    public Long getTrainingTypeId() {
        return trainingTypeId;
    }

    public void setTrainingTypeId(Long trainingTypeId) {
        this.trainingTypeId = trainingTypeId;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }
}