package com.example.gymcrm.dto.trainee;

import io.swagger.v3.oas.annotations.media.Schema;

public class TrainerSummary {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;

    public TrainerSummary(String username, String firstName, String lastName, String specialization) {
        this.username = username; this.firstName = firstName;
        this.lastName = lastName; this.specialization = specialization;
    }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
}