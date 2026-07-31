package com.example.gymcrm.dto.trainee;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

// Response for endpoints 5 & 6
public class TraineeProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
     private String address;
     private boolean isActive;
    private List<TrainerSummary> trainers;

    public TraineeProfileResponse(String username, String firstName, String lastName,
                                  LocalDate dateOfBirth, String address, boolean isActive,
                                  List<TrainerSummary> trainers) {
        this.username = username; this.firstName = firstName; this.lastName = lastName;
        this.dateOfBirth = dateOfBirth; this.address = address; this.isActive = isActive;
        this.trainers = trainers;
    }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public boolean isActive() { return isActive; }
    public List<TrainerSummary> getTrainers() { return trainers; }
}