package com.example.gymcrm.dto.trainer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class ActiveStatusRequest {
    @NotNull(message = "isActive is required")
    private Boolean isActive;

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}