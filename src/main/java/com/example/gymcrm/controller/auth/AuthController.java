package com.example.gymcrm.controller.auth;

import com.example.gymcrm.dto.auth.LoginRequest;
import com.example.gymcrm.dto.auth.LoginResponse;
import com.example.gymcrm.dto.error.ErrorResponse;
import com.example.gymcrm.security.JwtService;
import com.example.gymcrm.security.LoginAttemptService;
import com.example.gymcrm.security.TokenBlacklist;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Authentication", description = "Login and token issuance")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklist tokenBlacklist;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, LoginAttemptService loginAttemptService, TokenBlacklist tokenBlacklist) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Operation(summary = "Login — authenticate and receive a JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated; JWT returned",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "423", description = "Too many failed attempts",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            long secs = loginAttemptService.secondsUntilUnblock(username);
            throw new LockedException(
                    "Account locked due to too many failed attempts. Try again in " + secs + " seconds.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));

            loginAttemptService.recordSuccess(username);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException ex) {
            loginAttemptService.recordFailure(username);
            throw ex;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                long expiryMs = jwtService.extractExpiration(token).getTime();
                tokenBlacklist.blacklist(token, expiryMs);
            } catch (Exception e) {

            }
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}