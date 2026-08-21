package com.example.gymcrm.controller;

import com.example.gymcrm.controller.auth.AuthController;
import com.example.gymcrm.exceptions.GlobalExceptionHandler;
import com.example.gymcrm.security.JwtService;
import com.example.gymcrm.security.LoginAttemptService;
import com.example.gymcrm.security.TokenBlacklist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private TokenBlacklist tokenBlacklist;

    @InjectMocks private AuthController controller;

    private MockMvc mockMvc;

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "pass123";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /login")
    class Login {

        @Test
        @DisplayName("HAPPY: valid credentials returns 200 with JWT")
        void login_success() throws Exception {
            UserDetails userDetails = new User(USERNAME, PASSWORD,
                    List.of(new SimpleGrantedAuthority("ROLE_TRAINEE")));
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt.token.here");

            String body = """
                    { "username": "John.Doe", "password": "pass123" }
                    """;

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"));

            verify(loginAttemptService).recordSuccess(USERNAME);
        }

        @Test
        @DisplayName("UNHAPPY: invalid credentials returns 401 and records failure")
        void login_badCredentials() throws Exception {
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            String body = """
                    { "username": "John.Doe", "password": "wrong" }
                    """;

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());

            verify(loginAttemptService).recordFailure(USERNAME);
        }

        @Test
        @DisplayName("UNHAPPY: blocked user returns 423 Locked")
        void login_blocked() throws Exception {
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(true);
            when(loginAttemptService.secondsUntilUnblock(USERNAME)).thenReturn(120L);

            String body = """
                    { "username": "John.Doe", "password": "pass123" }
                    """;

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isLocked());   // 423

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("UNHAPPY: blank username returns 400")
        void login_blankUsername() throws Exception {
            String body = """
                    { "username": "", "password": "pass123" }
                    """;

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /logout")
    class Logout {

        @Test
        @DisplayName("HAPPY: returns 200 and blacklists token")
        void logout_success() throws Exception {
            when(jwtService.extractExpiration(anyString()))
                    .thenReturn(new java.util.Date(System.currentTimeMillis() + 100000));

            mockMvc.perform(post("/logout")
                            .header("Authorization", "Bearer some.jwt.token"))
                    .andExpect(status().isOk());

            verify(tokenBlacklist).blacklist(eq("some.jwt.token"), anyLong());
        }

        @Test
        @DisplayName("HAPPY: no token still returns 200")
        void logout_noToken() throws Exception {
            mockMvc.perform(post("/logout"))
                    .andExpect(status().isOk());

            verifyNoInteractions(tokenBlacklist);
        }
    }
}