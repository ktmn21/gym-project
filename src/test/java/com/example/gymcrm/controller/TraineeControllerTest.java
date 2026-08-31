package com.example.gymcrm.controller;

import com.example.gymcrm.controller.trainee.TraineeController;
import com.example.gymcrm.exceptions.GlobalExceptionHandler;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.model.*;
import com.example.gymcrm.security.SecurityUtils;
import com.example.gymcrm.service.TraineeService;
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
class TraineeControllerTest {

    @Mock private TraineeService service;
    @Mock private SecurityUtils securityUtils;   // ← ownership check mocked (no-op)

    @InjectMocks private TraineeController controller;

    private MockMvc mockMvc;

    private static final String USERNAME = "Test.Test";

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
        u.setUsername(username);
        u.setFirstName(first);
        u.setLastName(last);
        u.setActive(active);
        return u;
    }

    private TrainingType buildType(String name) {
        TrainingType t = new TrainingType();
        t.setTrainingTypeName(name);
        return t;
    }

    private Trainer buildTrainer(String username, String first, String last, String spec) {
        Trainer t = new Trainer();
        t.setUser(buildUser(username, first, last, true));
        t.setSpecialization(buildType(spec));
        t.setTrainees(new HashSet<>());
        return t;
    }

    private Trainee buildTrainee(boolean active, Trainer... trainers) {
        Trainee trainee = new Trainee();
        trainee.setUser(buildUser(USERNAME, "Test", "Test", active));
        trainee.setDateOfBirth(LocalDate.of(1995, 5, 20));
        trainee.setAddress("Bishkek");
        trainee.setTrainers(new HashSet<>(List.of(trainers)));
        return trainee;
    }

    @Nested
    @DisplayName("GET /trainee/{username}")
    class GetProfile {

        @Test
        @DisplayName("HAPPY: returns 200 with profile and trainers")
        void getProfile_success() throws Exception {
            Trainer trainer = buildTrainer("Jane.Smith", "Jane", "Smith", "Cardio");
            Trainee trainee = buildTrainee(true, trainer);

            when(service.selectByUsername(USERNAME)).thenReturn(trainee);

            mockMvc.perform(get("/trainee/{username}", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("Test"))
                    .andExpect(jsonPath("$.address").value("Bishkek"))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.trainers[0].username").value("Jane.Smith"))
                    .andExpect(jsonPath("$.trainers[0].specialization").value("Cardio"));

            verify(securityUtils).checkOwnership(USERNAME);
        }

        @Test
        @DisplayName("UNHAPPY: trainee not found returns 404")
        void getProfile_notFound() throws Exception {
            when(service.selectByUsername(USERNAME))
                    .thenThrow(new EntityNotFoundException("Trainee not found"));

            mockMvc.perform(get("/trainee/{username}", USERNAME))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /trainee/{username}")
    class UpdateProfile {

        @Test
        @DisplayName("HAPPY: returns 200 with updated profile")
        void update_success() throws Exception {
            Trainee trainee = buildTrainee(true);
            when(service.updateProfile(eq(USERNAME), eq("Jane"),
                    eq("Doe"), any(), any())).thenReturn(trainee);

            String body = """
                    {
                        "firstName": "Jane",
                        "lastName": "Doe",
                        "dateOfBirth": "1990-01-01",
                        "address": "New Address",
                        "isActive": true
                    }
                    """;

            mockMvc.perform(put("/trainee/{username}", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME));
        }

        @Test
        @DisplayName("UNHAPPY: missing firstName returns 400")
        void update_missingFirstName() throws Exception {
            String body = """
                    { "lastName": "Doe", "isActive": true }
                    """;

            mockMvc.perform(put("/trainee/{username}", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UNHAPPY: missing isActive returns 400")
        void update_missingIsActive() throws Exception {
            String body = """
                    { "firstName": "Jane", "lastName": "Doe" }
                    """;

            mockMvc.perform(put("/trainee/{username}", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /trainee/{username}")
    class Delete {

        @Test
        @DisplayName("HAPPY: returns 200")
        void delete_success() throws Exception {
            doNothing().when(service).deleteByUsername(USERNAME);

            mockMvc.perform(delete("/trainee/{username}", USERNAME))
                    .andExpect(status().isOk());

            verify(service).deleteByUsername(USERNAME);
        }

        @Test
        @DisplayName("UNHAPPY: not found returns 404")
        void delete_notFound() throws Exception {
            doThrow(new EntityNotFoundException("Trainee not found"))
                    .when(service).deleteByUsername(USERNAME);

            mockMvc.perform(delete("/trainee/{username}", USERNAME))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /trainee/{username}/trainers")
    class UpdateTrainers {

        @Test
        @DisplayName("HAPPY: returns 200 with trainer list")
        void updateTrainers_success() throws Exception {
            Trainer trainer = buildTrainer("Jane.Smith", "Jane", "Smith", "Cardio");
            Trainee trainee = buildTrainee(true, trainer);

            when(service.updateTrainersListByUsername(eq(USERNAME), anySet()))
                    .thenReturn(trainee);

            String body = """
                    { "trainerUsernames": ["Jane.Smith"] }
                    """;

            mockMvc.perform(put("/trainee/{username}/trainers", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username").value("Jane.Smith"))
                    .andExpect(jsonPath("$[0].specialization").value("Cardio"));
        }

        @Test
        @DisplayName("UNHAPPY: empty trainers list returns 400")
        void updateTrainers_empty() throws Exception {
            String body = """
                    { "trainerUsernames": [] }
                    """;

            mockMvc.perform(put("/trainee/{username}/trainers", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UNHAPPY: trainer not found returns 404")
        void updateTrainers_trainerNotFound() throws Exception {
            when(service.updateTrainersListByUsername(anyString(), anySet()))
                    .thenThrow(new EntityNotFoundException("Trainer not found"));

            String body = """
                    { "trainerUsernames": ["Ghost.Trainer"] }
                    """;

            mockMvc.perform(put("/trainee/{username}/trainers", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /trainee/{username}/trainings")
    class GetTrainings {

        @Test
        @DisplayName("HAPPY: returns 200 with trainings list")
        void getTrainings_success() throws Exception {
            Trainer trainer = buildTrainer("Jane.Smith", "Jane", "Smith", "Cardio");
            Training training = new Training();
            training.setTrainingName("Morning Cardio");
            training.setTrainingDate(LocalDate.of(2024, 5, 20));
            training.setTrainingType(buildType("Cardio"));
            training.setTrainingDuration(60);
            training.setTrainer(trainer);

            when(service.getTraineeTrainings(eq(USERNAME), any(), any(), any(), any()))
                    .thenReturn(List.of(training));

            mockMvc.perform(get("/trainee/{username}/trainings", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].trainingName").value("Morning Cardio"))
                    .andExpect(jsonPath("$[0].trainingType").value("Cardio"))
                    .andExpect(jsonPath("$[0].trainingDuration").value(60))
                    .andExpect(jsonPath("$[0].trainerName").value("Jane.Smith"));
        }

        @Test
        @DisplayName("HAPPY: with filters returns 200")
        void getTrainings_withFilters() throws Exception {
            when(service.getTraineeTrainings(eq(USERNAME),
                    any(), any(), eq("Jane"), eq("Cardio")))
                    .thenReturn(List.of());

            mockMvc.perform(get("/trainee/{username}/trainings", USERNAME)
                            .param("fromDate", "2024-01-01")
                            .param("toDate", "2024-12-31")
                            .param("trainerName", "Jane")
                            .param("trainingType", "Cardio"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /trainee/{username}/status")
    class SetActiveStatus {

        @Test
        @DisplayName("HAPPY: returns 200")
        void setStatus_success() throws Exception {
            doNothing().when(service).setActiveStatus(USERNAME, false);

            String body = """
                    { "isActive": false }
                    """;

            mockMvc.perform(patch("/trainee/{username}/status", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(service).setActiveStatus(USERNAME, false);
        }

        @Test
        @DisplayName("UNHAPPY: missing isActive returns 400")
        void setStatus_missingIsActive() throws Exception {
            mockMvc.perform(patch("/trainee/{username}/status", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ }"))
                    .andExpect(status().isBadRequest());
        }
    }
}