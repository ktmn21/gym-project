package com.example.gymcrm.controller.trainee;

import com.example.gymcrm.dto.TraineeRegistrationRequest;
import com.example.gymcrm.dto.TraineeRegistrationResponse;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.service.TraineeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trainee")
public class TraineeController {

    private final TraineeService service;

    public TraineeController(TraineeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TraineeRegistrationResponse> traineeRegistration(@Valid @RequestBody TraineeRegistrationRequest request){
        Trainee trainee = service.createProfile(request.getFirstName(), request.getLastname(), request.getDateOfBirth(), request.getAddress());

        TraineeRegistrationResponse response = new TraineeRegistrationResponse(trainee.getUser().getUsername(), trainee.getUser().getPassword());

        return ResponseEntity.ok(response);
    }
}
