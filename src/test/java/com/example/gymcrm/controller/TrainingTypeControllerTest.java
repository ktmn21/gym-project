package com.example.gymcrm.controller;

import com.example.gymcrm.controller.trainingtype.TrainingTypeController;
import com.example.gymcrm.exceptions.GlobalExceptionHandler;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock private TrainingTypeService service;
    @InjectMocks private TrainingTypeController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("HAPPY: returns 200 with training types")
    void getAll_success() throws Exception {
        TrainingType cardio = new TrainingType();
        cardio.setId(1L);
        cardio.setTrainingTypeName("Cardio");
        TrainingType yoga = new TrainingType();
        yoga.setId(2L);
        yoga.setTrainingTypeName("Yoga");

        when(service.getAll()).thenReturn(List.of(cardio, yoga));

        mockMvc.perform(get("/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingTypeId").value(1))
                .andExpect(jsonPath("$[0].trainingType").value("Cardio"))
                .andExpect(jsonPath("$[1].trainingType").value("Yoga"));
    }

    @Test
    @DisplayName("HAPPY: empty list returns 200 with empty array")
    void getAll_empty() throws Exception {
        when(service.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}