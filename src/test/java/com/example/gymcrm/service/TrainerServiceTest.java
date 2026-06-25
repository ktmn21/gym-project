package com.example.gymcrm.service;

import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerService unit tests")
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UsernamePasswordGenerator usernamePasswordGenerator;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService();
        trainerService.setUsernamePasswordGenerator(usernamePasswordGenerator);

        // inject mocked DAO into field (since TrainerService uses field injection)
        try {
            var field = TrainerService.class.getDeclaredField("trainerDao");
            field.setAccessible(true);
            field.set(trainerService, trainerDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("createTrainer: happy path assigns username/password/active and saves")
    void createTrainer_happyPath() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setSpecialization(1L);

        when(usernamePasswordGenerator.generateUserName("John", "Smith"))
                .thenReturn("John.Smith");
        when(usernamePasswordGenerator.generatePassword())
                .thenReturn("securePass123");

        Trainer savedFromDao = new Trainer();
        savedFromDao.setUserId(10L);
        savedFromDao.setFirstName("John");
        savedFromDao.setLastName("Smith");
        savedFromDao.setUsername("John.Smith");
        savedFromDao.setPassword("securePass123");
        savedFromDao.setActive(true);
        savedFromDao.setSpecialization(1L);
        when(trainerDao.save(any(Trainer.class))).thenReturn(savedFromDao);

        Trainer result = trainerService.createTrainer(trainer);

        verify(usernamePasswordGenerator).generateUserName("John", "Smith");
        verify(usernamePasswordGenerator).generatePassword();

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).save(captor.capture());
        Trainer passedToDao = captor.getValue();

        assertEquals("John.Smith", passedToDao.getUsername());
        assertEquals("securePass123", passedToDao.getPassword());
        assertTrue(passedToDao.isActive());

        assertEquals(10L, result.getUserId());
        assertEquals("John.Smith", result.getUsername());
    }

    @Test
    @DisplayName("createTrainer: username collision handled by generator")
    void createTrainer_usernameCollisionHandled() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Smith");

        // generator already implements collision logic; we just simulate the resolved username
        when(usernamePasswordGenerator.generateUserName("John", "Smith"))
                .thenReturn("John.Smith.1");
        when(usernamePasswordGenerator.generatePassword())
                .thenReturn("password456");

        Trainer savedFromDao = new Trainer();
        savedFromDao.setUserId(11L);
        savedFromDao.setUsername("John.Smith.1");
        savedFromDao.setPassword("password456");
        savedFromDao.setActive(true);
        when(trainerDao.save(any(Trainer.class))).thenReturn(savedFromDao);

        Trainer result = trainerService.createTrainer(trainer);

        assertEquals("John.Smith.1", result.getUsername());
        assertEquals(11L, result.getUserId());
        assertTrue(result.isActive());
        verify(usernamePasswordGenerator).generateUserName("John", "Smith");
    }

    @Test
    @DisplayName("updateTrainer: happy path updates existing trainer")
    void updateTrainer_happyPath() {
        Trainer existing = new Trainer();
        existing.setUserId(5L);
        existing.setFirstName("Alice");
        existing.setLastName("Brown");
        existing.setUsername("Alice.Brown");
        existing.setActive(true);
        existing.setSpecialization(1L);

        when(trainerDao.findById(5L)).thenReturn(Optional.of(existing));
        when(trainerDao.update(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        existing.setSpecialization(2L);
        existing.setActive(false);

        Trainer result = trainerService.updateTrainer(existing);

        verify(trainerDao).findById(5L);
        verify(trainerDao).update(existing);

        assertEquals(2L, result.getSpecialization());
        assertFalse(result.isActive());
    }

    @Test
    @DisplayName("updateTrainer: unhappy path throws when trainer not found")
    void updateTrainer_unhappyPath_notFound() {
        Trainer trainer = new Trainer();
        trainer.setUserId(999L);

        when(trainerDao.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trainerService.updateTrainer(trainer)
        );

        assertEquals("Trainer not found: id=999", ex.getMessage());
        verify(trainerDao).findById(999L);
        verify(trainerDao, never()).update(any());
    }

    @Test
    @DisplayName("selectTrainer: happy path returns trainer")
    void selectTrainer_happyPath() {
        Trainer existing = new Trainer();
        existing.setUserId(3L);

        when(trainerDao.findById(3L)).thenReturn(Optional.of(existing));

        Optional<Trainer> result = trainerService.selectTrainer(3L);

        assertTrue(result.isPresent());
        assertEquals(3L, result.get().getUserId());
        verify(trainerDao).findById(3L);
    }

    @Test
    @DisplayName("selectTrainer: unhappy path returns empty when not found")
    void selectTrainer_unhappyPath_notFound() {
        when(trainerDao.findById(8L)).thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.selectTrainer(8L);

        assertFalse(result.isPresent());
        verify(trainerDao).findById(8L);
    }

    @Test
    @DisplayName("selectTrainerByUsername delegates to DAO and returns result")
    void selectTrainerByUsername() {
        Trainer trainer = new Trainer();
        trainer.setUsername("John.Smith");

        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.selectTrainerByUsername("John.Smith");

        assertTrue(result.isPresent());
        assertEquals("John.Smith", result.get().getUsername());
        verify(trainerDao).findByUsername("John.Smith");
    }

    @Test
    @DisplayName("selectAllTrainers delegates to DAO and returns list")
    void selectAllTrainers() {
        Trainer t1 = new Trainer();
        Trainer t2 = new Trainer();

        when(trainerDao.findAll()).thenReturn(List.of(t1, t2));

        List<Trainer> result = trainerService.selectAllTrainers();

        assertEquals(2, result.size());
        verify(trainerDao).findAll();
    }
}