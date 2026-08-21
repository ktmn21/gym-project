package com.example.gymcrm.controller.trainer;

import com.example.gymcrm.dto.error.ErrorResponse;
import com.example.gymcrm.dto.trainer.*;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trainer")
@Tag(name = "Trainer", description = "Trainer registration and management")
public class TrainerController {

    private final TrainerService service;

    public TrainerController(TrainerService service) {
        this.service = service;
    }

    @Operation(summary = "Register a new trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registered",
                    content = @Content(schema = @Schema(implementation = TrainerRegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Specialization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TrainerRegistrationResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        Trainer trainer = service.createProfile(
                request.getFirstName(), request.getLastName(), request.getSpecializationId());
        return ResponseEntity.ok(new TrainerRegistrationResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getRawPassword()
        ));
    }

    @Operation(summary = "Get trainer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @PathVariable("username") String username,
            @RequestHeader("X-Password") String password) {
        Trainer trainer = service.selectByUsername(username, password);
        return ResponseEntity.ok(TrainerMapper.toProfileResponse(trainer));
    }

    @Operation(summary = "Update trainer profile (specialization is read-only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            @PathVariable("username") String username,
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody TrainerUpdateRequest request) {

        Trainer trainer = service.updateProfile(username, password,
                request.getFirstName(), request.getLastName(), null);

        if (request.getIsActive() != null
                && request.getIsActive() != trainer.getUser().isActive()) {
            service.setActiveStatus(username, password, request.getIsActive());
            trainer.getUser().setActive(request.getIsActive());
        }

        return ResponseEntity.ok(TrainerMapper.toProfileResponse(trainer));
    }

    @Operation(summary = "Get trainer trainings list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings retrieved",
                    content = @Content(schema = @Schema(implementation = TrainerTrainingResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(
            @PathVariable("username") String username,
            @RequestHeader("X-Password") String password,
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "traineeName", required = false) String traineeName) {

        List<TrainerTrainingResponse> trainings = service.getTrainerTrainings(
                        username, password, fromDate, toDate, traineeName)
                .stream().map(TrainerMapper::toTrainingResponse).collect(Collectors.toList());
        return ResponseEntity.ok(trainings);
    }

    @Operation(summary = "Activate or deactivate trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> setActiveStatus(
            @PathVariable("username") String username,
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody ActiveStatusRequest request) {
        service.setActiveStatus(username, password, request.getIsActive());
        return ResponseEntity.ok().build();
    }
}