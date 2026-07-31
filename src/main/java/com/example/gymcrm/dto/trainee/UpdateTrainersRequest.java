package com.example.gymcrm.dto.trainee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class UpdateTrainersRequest {
    @NotEmpty(message = "Trainers list is required")
    private Set<String> trainerUsernames;

    public Set<String> getTrainerUsernames() { return trainerUsernames; }
    public void setTrainerUsernames(Set<String> trainerUsernames) { this.trainerUsernames = trainerUsernames; }
}