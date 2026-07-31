package com.example.gymcrm.controller.auth;

import com.example.gymcrm.dto.auth.ChangePasswordRequest;
import com.example.gymcrm.dto.error.ErrorResponse;
import com.example.gymcrm.service.AuthenticationService;
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
@Tag(name = "Authentication", description = "Login and password management")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login — verify username and password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credentials valid"),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestHeader("X-Password") String password) {
        authService.authenticate(username, password);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change login password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/login/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable("username") String username,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}