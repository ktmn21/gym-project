package com.example.gymcrm.model;

public class Trainer extends User {
    private Long userId;
    private Long specialization;

    public Trainer() {
    }

    public Trainer(Long userId, String firstName, String lastName,
                   String username, String password, boolean active,
                   Long specialization) {
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

    public Long getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Long specialization) {
        this.specialization = specialization;
    }
}