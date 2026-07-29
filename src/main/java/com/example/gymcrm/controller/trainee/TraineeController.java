package com.example.gymcrm.controller.trainee;

import com.example.gymcrm.dto.trainee.TraineeRegistrationRequest;
import com.example.gymcrm.dto.trainee.TraineeRegistrationResponse;
import com.example.gymcrm.dto.error.ErrorResponse;
import com.example.gymcrm.dto.trainee.*;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trainee")
@Tag(name = "Trainee", description = "Trainee registration and management")
public class TraineeController {

    private final TraineeService service;

    public TraineeController(TraineeService service) {
        this.service = service;
    }

    @Operation(summary = "Register a new trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registered",
                    content = @Content(schema = @Schema(implementation = TraineeRegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TraineeRegistrationResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        Trainee trainee = service.createProfile(request.getFirstName(), request.getLastname(),
                request.getDateOfBirth(), request.getAddress());
        return ResponseEntity.ok(new TraineeRegistrationResponse(
                trainee.getUser().getUsername(), trainee.getUser().getPassword()));
    }

    @Operation(summary = "Get trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @PathVariable String username,
            @RequestHeader("X-Password") String password) {
        Trainee trainee = service.selectByUsername(username, password);
        return ResponseEntity.ok(TraineeMapper.toProfileResponse(trainee));
    }

    @Operation(summary = "Update trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> updateProfile(
            @PathVariable String username,
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody TraineeUpdateRequest request) {

        Trainee trainee = service.updateProfile(username, password,
                request.getFirstName(), request.getLastName(),
                request.getDateOfBirth(), request.getAddress());

        if (request.getIsActive() != null
                && request.getIsActive() != trainee.getUser().isActive()) {
            service.setActiveStatus(username, password, request.getIsActive());
            trainee.getUser().setActive(request.getIsActive());
        }

        return ResponseEntity.ok(TraineeMapper.toProfileResponse(trainee));
    }

    @Operation(summary = "Delete trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(
            @PathVariable String username,
            @RequestHeader("X-Password") String password) {
        service.deleteByUsername(username, password);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update trainee's trainer list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainers updated",
                    content = @Content(schema = @Schema(implementation = TrainerSummary.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerSummary>> updateTrainers(
            @PathVariable String username,
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody UpdateTrainersRequest request) {

        Trainee trainee = service.updateTrainersListByUsername(
                username, password, request.getTrainerUsernames());

        List<TrainerSummary> trainers = trainee.getTrainers().stream()
                .map(TraineeMapper::toTrainerSummary).collect(Collectors.toList());
        return ResponseEntity.ok(trainers);
    }

    @Operation(summary = "Get trainee trainings list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings retrieved",
                    content = @Content(schema = @Schema(implementation = TraineeTrainingResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestHeader("X-Password") String password,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        List<TraineeTrainingResponse> trainings = service.getTraineeTrainings(
                        username, password, fromDate, toDate, trainerName, trainingType)
                .stream().map(TraineeMapper::toTrainingResponse).collect(Collectors.toList());
        return ResponseEntity.ok(trainings);
    }

    @Operation(summary = "Activate or deactivate trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> setActiveStatus(
            @PathVariable String username,
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody ActiveStatusRequest request) {
        service.setActiveStatus(username, password, request.getIsActive());
        return ResponseEntity.ok().build();
    }
}