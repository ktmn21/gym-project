package com.example.gymcrm.controller.training;

import com.example.gymcrm.dto.error.ErrorResponse;
import com.example.gymcrm.dto.training.AddTrainingRequest;
import com.example.gymcrm.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/training")
@Tag(name = "Training", description = "Training management")
public class TrainingController {

    private final TrainingService service;

    public TrainingController(TrainingService service) {
        this.service = service;
    }

    @Operation(summary = "Add a training session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training added"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody AddTrainingRequest request) {

        service.addTraining(
                request.getTraineeUsername(), password,
                request.getTrainerUsername(),
                request.getTrainingName(),
                request.getTrainingDate(),
                request.getTrainingDuration());

        return ResponseEntity.ok().build();
    }
}