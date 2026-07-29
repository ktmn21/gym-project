package com.example.gymcrm.controller;

import com.example.gymcrm.controller.trainer.TrainerController;
import com.example.gymcrm.exceptions.GlobalExceptionHandler;
import com.example.gymcrm.exceptions.AuthenticationException;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.model.*;
import com.example.gymcrm.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock private TrainerService service;
    @InjectMocks private TrainerController controller;

    private MockMvc mockMvc;

    private static final String USERNAME = "Jane.Smith";
    private static final String PASSWORD = "pass1234";

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    private User buildUser(String username, String first, String last, boolean active) {
        User u = new User();
        u.setUsername(username); u.setFirstName(first); u.setLastName(last); u.setActive(active);
        return u;
    }
    private TrainingType buildType(String name) {
        TrainingType t = new TrainingType(); t.setTrainingTypeName(name); return t;
    }
    private Trainee buildTrainee(String username, String first, String last) {
        Trainee t = new Trainee(); t.setUser(buildUser(username, first, last, true)); return t;
    }
    private Trainer buildTrainer(boolean active, Trainee... trainees) {
        Trainer trainer = new Trainer();
        trainer.setUser(buildUser(USERNAME, "Jane", "Smith", active));
        trainer.setSpecialization(buildType("Cardio"));
        trainer.setTrainees(new HashSet<>(List.of(trainees)));
        return trainer;
    }

    @Nested
    @DisplayName("POST /trainer")
    class Register {
        @Test
        @DisplayName("HAPPY: returns 200 with credentials")
        void register_success() throws Exception {
            Trainer trainer = buildTrainer(true);
            trainer.getUser().setPassword("genPass1");
            when(service.createProfile(eq("Jane"), eq("Smith"), eq(1L))).thenReturn(trainer);

            String body = """
                    { "firstName": "Jane", "lastName": "Smith", "specializationId": 1 }
                    """;

            mockMvc.perform(post("/trainer")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.password").value("genPass1"));
        }

        @Test
        @DisplayName("UNHAPPY: missing firstName returns 400")
        void register_missingFirstName() throws Exception {
            String body = """
                    { "lastName": "Smith", "specializationId": 1 }
                    """;
            mockMvc.perform(post("/trainer")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UNHAPPY: missing specializationId returns 400")
        void register_missingSpecialization() throws Exception {
            String body = """
                    { "firstName": "Jane", "lastName": "Smith" }
                    """;
            mockMvc.perform(post("/trainer")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UNHAPPY: specialization not found returns 404")
        void register_specNotFound() throws Exception {
            when(service.createProfile(anyString(), anyString(), anyLong()))
                    .thenThrow(new EntityNotFoundException("TrainingType not found"));
            String body = """
                    { "firstName": "Jane", "lastName": "Smith", "specializationId": 99 }
                    """;
            mockMvc.perform(post("/trainer")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /trainer/{username}")
    class GetProfile {
        @Test
        @DisplayName("HAPPY: returns 200 with profile and trainees")
        void getProfile_success() throws Exception {
            Trainee trainee = buildTrainee("John.Doe", "John", "Doe");
            Trainer trainer = buildTrainer(true, trainee);
            when(service.selectByUsername(USERNAME, PASSWORD)).thenReturn(trainer);

            mockMvc.perform(get("/trainer/{username}", USERNAME)
                            .header("X-Password", PASSWORD))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.specialization").value("Cardio"))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.trainees[0].username").value("John.Doe"));
        }

        @Test
        @DisplayName("UNHAPPY: auth fails returns 401")
        void getProfile_authFails() throws Exception {
            when(service.selectByUsername(USERNAME, PASSWORD))
                    .thenThrow(new AuthenticationException("Invalid credentials"));
            mockMvc.perform(get("/trainer/{username}", USERNAME)
                            .header("X-Password", PASSWORD))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UNHAPPY: not found returns 404")
        void getProfile_notFound() throws Exception {
            when(service.selectByUsername(USERNAME, PASSWORD))
                    .thenThrow(new EntityNotFoundException("Trainer not found"));
            mockMvc.perform(get("/trainer/{username}", USERNAME)
                            .header("X-Password", PASSWORD))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /trainer/{username}")
    class UpdateProfile {
        @Test
        @DisplayName("HAPPY: returns 200 with updated profile")
        void update_success() throws Exception {
            Trainer trainer = buildTrainer(true);
            when(service.updateProfile(eq(USERNAME), eq(PASSWORD), eq("Janet"), eq("Smith"), isNull()))
                    .thenReturn(trainer);

            String body = """
                    { "firstName": "Janet", "lastName": "Smith", "specializationId": 1, "isActive": true }
                    """;
            mockMvc.perform(put("/trainer/{username}", USERNAME)
                            .header("X-Password", PASSWORD)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME));
        }

        @Test
        @DisplayName("UNHAPPY: missing firstName returns 400")
        void update_missingFirstName() throws Exception {
            String body = """
                    { "lastName": "Smith", "isActive": true }
                    """;
            mockMvc.perform(put("/trainer/{username}", USERNAME)
                            .header("X-Password", PASSWORD)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UNHAPPY: missing isActive returns 400")
        void update_missingIsActive() throws Exception {
            String body = """
                    { "firstName": "Janet", "lastName": "Smith" }
                    """;
            mockMvc.perform(put("/trainer/{username}", USERNAME)
                            .header("X-Password", PASSWORD)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UNHAPPY: auth fails returns 401")
        void update_authFails() throws Exception {
            when(service.updateProfile(anyString(), anyString(), anyString(), anyString(), any()))
                    .thenThrow(new AuthenticationException("Invalid credentials"));
            String body = """
                    { "firstName": "Janet", "lastName": "Smith", "isActive": true }
                    """;
            mockMvc.perform(put("/trainer/{username}", USERNAME)
                            .header("X-Password", PASSWORD)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /trainer/{username}/trainings")
    class GetTrainings {
        @Test
        @DisplayName("HAPPY: returns 200 with trainings")
        void getTrainings_success() throws Exception {
            Trainee trainee = buildTrainee("John.Doe", "John", "Doe");
            Training training = new Training();
            training.setTrainingName("Morning Cardio");
            training.setTrainingDate(LocalDate.of(2024, 5, 20));
            training.setTrainingType(buildType("Cardio"));
            training.setTrainingDuration(60);
            training.setTrainee(trainee);

            when(service.getTrainerTrainings(eq(USERNAME), eq(PASSWORD), any(), any(), any()))
                    .thenReturn(List.of(training));

            mockMvc.perform(get("/trainer/{username}/trainings", USERNAME)
                            .header("X-Password", PASSWORD))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].trainingName").value("Morning Cardio"))
                    .andExpect(jsonPath("$[0].trainingType").value("Cardio"))
                    .andExpect(jsonPath("$[0].trainingDuration").value(60))
                    .andExpect(jsonPath("$[0].traineeName").value("John.Doe"));
        }

        @Test
        @DisplayName("HAPPY: with filters returns 200")
        void getTrainings_withFilters() throws Exception {
            when(service.getTrainerTrainings(eq(USERNAME), eq(PASSWORD), any(), any(), eq("John")))
                    .thenReturn(List.of());
            mockMvc.perform(get("/trainer/{username}/trainings", USERNAME)
                            .header("X-Password", PASSWORD)
                            .param("fromDate", "2024-01-01")
                            .param("toDate", "2024-12-31")
                            .param("traineeName", "John"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("UNHAPPY: auth fails returns 401")
        void getTrainings_authFails() throws Exception {
            when(service.getTrainerTrainings(anyString(), anyString(), any(), any(), any()))
                    .thenThrow(new AuthenticationException("Invalid credentials"));
            mockMvc.perform(get("/trainer/{username}/trainings", USERNAME)
                            .header("X-Password", PASSWORD))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /trainer/{username}/status")
    class SetActiveStatus {
        @Test
        @DisplayName("HAPPY: returns 200")
        void setStatus_success() throws Exception {
            doNothing().when(service).setActiveStatus(USERNAME, PASSWORD, false);
            String body = """
                    { "isActive": false }
                    """;
            mockMvc.perform(patch("/trainer/{username}/status", USERNAME)
                            .header("X-Password", PASSWORD)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk());
            verify(service).setActiveStatus(USERNAME, PASSWORD, false);
        }

        @Test
        @DisplayName("UNHAPPY: missing isActive returns 400")
        void setStatus_missing() throws Exception {
            mockMvc.perform(patch("/trainer/{username}/status", USERNAME)
                            .header("X-Password", PASSWORD)
                            .contentType(MediaType.APPLICATION_JSON).content("{ }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UNHAPPY: auth fails returns 401")
        void setStatus_authFails() throws Exception {
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(service).setActiveStatus(anyString(), anyString(), anyBoolean());
            String body = """
                    { "isActive": true }
                    """;
            mockMvc.perform(patch("/trainer/{username}/status", USERNAME)
                            .header("X-Password", PASSWORD)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
    }
}