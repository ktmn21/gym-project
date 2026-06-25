package com.example.gymcrm.dao;

import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoImplTest {

    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void setUp() {
        trainingDao = new TrainingDaoImpl();
        trainingDao.setStorage(new InMemoryStorage());
    }

    @Test
    @DisplayName("save should assign id and store training")
    void save_shouldAssignIdAndStoreTraining() {
        Training training = new Training(
                null,          // id will be generated
                10L,           // traineeId
                20L,           // trainerId
                "Morning Cardio",
                TrainingType.CARDIO,
                LocalDate.of(2024, 1, 10),
                60               // duration
        );

        trainingDao.save(training);

        assertNotNull(training.getId());
        assertEquals(1L, training.getId());

        Optional<Training> saved = trainingDao.findById(training.getId());
        assertTrue(saved.isPresent());
        assertEquals("Morning Cardio", saved.get().getTrainingName());
        assertEquals(60, saved.get().getTrainingDuration());
        assertEquals(10L, saved.get().getTraineeId());
        assertEquals(20L, saved.get().getTrainerId());
    }

    @Test
    @DisplayName("findById should return empty when training does not exist")
    void findById_shouldReturnEmptyWhenNotFound() {
        Optional<Training> result = trainingDao.findById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findAll should return all stored trainings")
    void findAll_shouldReturnAllStoredTrainings() {
        Training t1 = new Training(
                null,
                1L,
                2L,
                "Java Basics",
                TrainingType.CARDIO,
                LocalDate.of(2024, 2, 15),
                90
        );

        Training t2 = new Training(
                null,
                3L,
                4L,
                "Spring Core",
                TrainingType.CARDIO,
                LocalDate.of(2024, 3, 20),
                120
        );

        trainingDao.save(t1);
        trainingDao.save(t2);

        List<Training> all = trainingDao.findAll();

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(t -> "Java Basics".equals(t.getTrainingName())));
        assertTrue(all.stream().anyMatch(t -> "Spring Core".equals(t.getTrainingName())));
    }
}