package com.example.gymcrm.model;

public class Trainer extends User {
    private Long userId;
    private TrainingType specialization;

    public Trainer() {
    }

    public Trainer(Long userId, String firstName, String lastName,
                   String username, String password, boolean active,
                   TrainingType specialization) {
        super(firstName, lastName, username, password, active);
        this.userId = userId;
        this.specialization = specialization;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }
}