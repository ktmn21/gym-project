package com.example.gymcrm.service;

import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingService unit tests")
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingService();

        // inject mocked DAO into field (because TrainingService uses field injection)
        try {
            var field = TrainingService.class.getDeclaredField("trainingDao");
            field.setAccessible(true);
            field.set(trainingService, trainingDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("selectTraining: happy path returns training when found")
    void selectTraining_happyPath() {
        Training training = new Training();
        training.setId(1L);
        training.setTrainingName("Java Basics");
        training.setTraineeId(10L);
        training.setTrainerId(20L);
        training.setTrainingDate(LocalDate.of(2024, 1, 10));
        training.setTrainingDuration(90);

        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.selectTraining(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Java Basics", result.get().getTrainingName());
        verify(trainingDao).findById(1L);
    }

    @Test
    @DisplayName("selectTraining: unhappy path returns empty when not found")
    void selectTraining_unhappyPath_notFound() {
        when(trainingDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Training> result = trainingService.selectTraining(999L);

        assertFalse(result.isPresent());
        verify(trainingDao).findById(999L);
    }

    @Test
    @DisplayName("createTraining: happy path delegates to DAO and returns saved training")
    void createTraining_happyPath() {
        Training training = new Training();
        training.setTrainingName("Spring Core");
        training.setTraineeId(10L);
        training.setTrainerId(20L);
        training.setTrainingDate(LocalDate.of(2024, 3, 1));
        training.setTrainingDuration(120);

        Training saved = new Training();
        saved.setId(5L);
        saved.setTrainingName("Spring Core");
        saved.setTraineeId(10L);
        saved.setTrainerId(20L);
        saved.setTrainingDate(training.getTrainingDate());
        saved.setTrainingDuration(120);

        when(trainingDao.save(training)).thenReturn(saved);

        Training result = trainingService.createTraining(training);

        verify(trainingDao).save(training);
        assertEquals(5L, result.getId());
        assertEquals("Spring Core", result.getTrainingName());
        assertEquals(10L, result.getTraineeId());
        assertEquals(20L, result.getTrainerId());
    }

    @Test
    @DisplayName("electAllTrainings: returns all trainings from DAO")
    void electAllTrainings_returnsAll() {
        Training t1 = new Training();
        t1.setId(1L);
        Training t2 = new Training();
        t2.setId(2L);

        when(trainingDao.findAll()).thenReturn(List.of(t1, t2));

        List<Training> result = trainingService.electAllTrainings();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(trainingDao).findAll();
    }
}