package com.example.gymcrm.controller;

import com.example.gymcrm.controller.training.TrainingController;
import com.example.gymcrm.exceptions.GlobalExceptionHandler;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.service.TrainingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock private TrainingService service;
    @InjectMocks private TrainingController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    @Test
    @DisplayName("HAPPY: valid training returns 200")
    void add_success() throws Exception {
        when(service.addTraining(anyString(), anyString(), anyString(),
                anyString(), any(), anyInt())).thenReturn(new Training());

        String body = """
                {
                    "traineeUsername": "John.Doe",
                    "trainerUsername": "Jane.Smith",
                    "trainingName": "Morning Cardio",
                    "trainingTypeName": "Cardio",
                    "trainingDate": "2024-05-20",
                    "trainingDuration": 60
                }
                """;

        mockMvc.perform(post("/training")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("UNHAPPY: missing trainerUsername returns 400")
    void add_missingTrainer() throws Exception {
        String body = """
                {
                    "traineeUsername": "John.Doe",
                    "trainingName": "Morning Cardio",
                    "trainingTypeName": "Cardio",
                    "trainingDate": "2024-05-20",
                    "trainingDuration": 60
                }
                """;
        mockMvc.perform(post("/training")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UNHAPPY: missing trainingTypeName returns 400")
    void add_missingType() throws Exception {
        String body = """
                {
                    "traineeUsername": "John.Doe",
                    "trainerUsername": "Jane.Smith",
                    "trainingName": "Morning Cardio",
                    "trainingDate": "2024-05-20",
                    "trainingDuration": 60
                }
                """;
        mockMvc.perform(post("/training")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UNHAPPY: negative duration returns 400")
    void add_negativeDuration() throws Exception {
        String body = """
                {
                    "traineeUsername": "John.Doe",
                    "trainerUsername": "Jane.Smith",
                    "trainingName": "Morning Cardio",
                    "trainingTypeName": "Cardio",
                    "trainingDate": "2024-05-20",
                    "trainingDuration": -5
                }
                """;
        mockMvc.perform(post("/training")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UNHAPPY: trainer not found returns 404")
    void add_notFound() throws Exception {
        when(service.addTraining(anyString(), anyString(), anyString(),
                anyString(), any(), anyInt()))
                .thenThrow(new EntityNotFoundException("Trainer not found"));

        String body = """
                {
                    "traineeUsername": "John.Doe",
                    "trainerUsername": "Ghost.Trainer",
                    "trainingName": "Morning Cardio",
                    "trainingTypeName": "Cardio",
                    "trainingDate": "2024-05-20",
                    "trainingDuration": 60
                }
                """;
        mockMvc.perform(post("/training")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }
}