package com.example.gymcrm.controller;

import com.example.gymcrm.controller.auth.AuthController;
import com.example.gymcrm.exceptions.GlobalExceptionHandler;
import com.example.gymcrm.exceptions.AuthenticationException;
import com.example.gymcrm.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationService authService;
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
    @DisplayName("GET /login")
    class Login {
        @Test
        @DisplayName("HAPPY: valid credentials returns 200")
        void login_success() throws Exception {
            doNothing().when(authService).authenticate(USERNAME, PASSWORD);

            mockMvc.perform(get("/login")
                            .param("username", USERNAME)
                            .header("X-Password", PASSWORD))
                    .andExpect(status().isOk());

            verify(authService).authenticate(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("UNHAPPY: invalid credentials returns 401")
        void login_authFails() throws Exception {
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authService).authenticate(USERNAME, PASSWORD);

            mockMvc.perform(get("/login")
                            .param("username", USERNAME)
                            .header("X-Password", PASSWORD))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /login/{username}/password")
    class ChangePassword {
        @Test
        @DisplayName("HAPPY: valid change returns 200")
        void change_success() throws Exception {
            doNothing().when(authService).changePassword(USERNAME, "old", "new");

            String body = """
                    { "oldPassword": "old", "newPassword": "new" }
                    """;

            mockMvc.perform(put("/login/{username}/password", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk());

            verify(authService).changePassword(USERNAME, "old", "new");
        }

        @Test
        @DisplayName("UNHAPPY: missing newPassword returns 400")
        void change_missingNew() throws Exception {
            String body = """
                    { "oldPassword": "old" }
                    """;

            mockMvc.perform(put("/login/{username}/password", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UNHAPPY: wrong old password returns 401")
        void change_wrongOld() throws Exception {
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authService).changePassword(anyString(), anyString(), anyString());

            String body = """
                    { "oldPassword": "wrong", "newPassword": "new" }
                    """;

            mockMvc.perform(put("/login/{username}/password", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
    }
}