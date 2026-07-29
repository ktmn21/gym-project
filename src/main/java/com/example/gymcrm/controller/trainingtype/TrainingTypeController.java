package com.example.gymcrm.controller.trainingtype;

import com.example.gymcrm.dto.trainingtype.TrainingTypeResponse;
import com.example.gymcrm.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/training-types")
@Tag(name = "Training Types", description = "Reference data for training types")
public class TrainingTypeController {

    private final TrainingTypeService service;

    public TrainingTypeController(TrainingTypeService service) {
        this.service = service;
    }

    @Operation(summary = "Get all training types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training types retrieved",
                    content = @Content(schema = @Schema(implementation = TrainingTypeResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<TrainingTypeResponse>> getAll() {
        List<TrainingTypeResponse> types = service.getAll().stream()
                .map(t -> new TrainingTypeResponse(t.getId(), t.getTrainingTypeName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }
}