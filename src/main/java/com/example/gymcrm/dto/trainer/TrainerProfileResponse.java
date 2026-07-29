package com.example.gymcrm.dto.trainer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class TrainerProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private boolean isActive;
    private List<TraineeSummary> trainees;

    public TrainerProfileResponse(String username, String firstName, String lastName,
                                  String specialization, boolean isActive, List<TraineeSummary> trainees) {
        this.username = username; this.firstName = firstName; this.lastName = lastName;
        this.specialization = specialization; this.isActive = isActive; this.trainees = trainees;
    }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
    public boolean isActive() { return isActive; }
    public List<TraineeSummary> getTrainees() { return trainees; }
}