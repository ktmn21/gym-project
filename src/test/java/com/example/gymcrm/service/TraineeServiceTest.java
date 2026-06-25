package com.example.gymcrm.service;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeService unit tests")
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UsernamePasswordGenerator usernamePasswordGenerator;

    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeService();
        traineeService.setUsernamePasswordGenerator(usernamePasswordGenerator);

        try {
            var field = TraineeService.class.getDeclaredField("traineeDao");
            field.setAccessible(true);
            field.set(traineeService, traineeDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("createTrainee: happy path assigns username, password, active=true and saves")
    void createTrainee_happyPath() {
        Trainee trainee = new Trainee(
                null,
                "John",
                "Smith",
                null,
                null,
                false,
                LocalDate.of(2000, 1, 1),
                "Bishkek"
        );

        when(usernamePasswordGenerator.generateUserName("John", "Smith"))
                .thenReturn("John.Smith");
        when(usernamePasswordGenerator.generatePassword())
                .thenReturn("randomPass");
        Trainee savedFromDao = new Trainee(
                1L,
                "John",
                "Smith",
                "John.Smith",
                "randomPass",
                true,
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
        when(traineeDao.save(any(Trainee.class))).thenReturn(savedFromDao);

        Trainee result = traineeService.createTrainee(trainee);

        // verify generator calls
        verify(usernamePasswordGenerator).generateUserName("John", "Smith");
        verify(usernamePasswordGenerator).generatePassword();

        // verify DAO is called with trainee having username/password/active=true
        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao).save(captor.capture());
        Trainee passedToDao = captor.getValue();

        assertEquals("John.Smith", passedToDao.getUsername());
        assertEquals("randomPass", passedToDao.getPassword());
        assertTrue(passedToDao.isActive());

        // verify service returns saved trainee from DAO
        assertEquals(1L, result.getUserId());
        assertEquals("John.Smith", result.getUsername());
    }

    @Test
    @DisplayName("createTrainee: username collision handled by generator (serial number suffix)")
    void createTrainee_usernameCollisionHandled() {
        Trainee trainee = new Trainee(
                null,
                "John",
                "Smith",
                null,
                null,
                false,
                LocalDate.of(2000, 1, 1),
                "Bishkek"
        );

        // Simulate generator resolving collision to John.Smith1
        when(usernamePasswordGenerator.generateUserName("John", "Smith"))
                .thenReturn("John.Smith1");
        when(usernamePasswordGenerator.generatePassword())
                .thenReturn("password123");
        Trainee savedFromDao = new Trainee(
                2L,
                "John",
                "Smith",
                "John.Smith1",
                "password123",
                true,
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
        when(traineeDao.save(any(Trainee.class))).thenReturn(savedFromDao);

        Trainee result = traineeService.createTrainee(trainee);

        verify(usernamePasswordGenerator).generateUserName("John", "Smith");

        assertEquals("John.Smith1", result.getUsername());
        assertEquals(2L, result.getUserId());
        assertTrue(result.isActive());
    }

    @Test
    @DisplayName("updateTrainee: happy path updates existing trainee")
    void updateTrainee_happyPath() {
        Trainee existing = new Trainee(
                1L,
                "Alice",
                "Brown",
                "Alice.Brown",
                "pass123",
                true,
                LocalDate.of(1999, 5, 5),
                "Osh"
        );

        when(traineeDao.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        existing.setAddress("Bishkek");
        existing.setActive(false);

        Trainee result = traineeService.updateTrainee(existing);

        verify(traineeDao).findById(1L);
        verify(traineeDao).update(existing);
        assertEquals("Bishkek", result.getAddress());
        assertFalse(result.isActive());
    }

    @Test
    @DisplayName("updateTrainee: unhappy path throws when trainee not found")
    void updateTrainee_unhappyPath_notFound() {
        Trainee trainee = new Trainee();
        trainee.setUserId(999L);

        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.updateTrainee(trainee)
        );

        assertEquals("Trainee not found", ex.getMessage());
        verify(traineeDao).findById(999L);
        verify(traineeDao, never()).update(any());
    }

    @Test
    @DisplayName("deleteTrainee: happy path deletes existing trainee")
    void deleteTrainee_happyPath() {
        Trainee existing = new Trainee();
        existing.setUserId(5L);

        when(traineeDao.findById(5L)).thenReturn(Optional.of(existing));

        traineeService.deleteTrainee(5L);

        verify(traineeDao).findById(5L);
        verify(traineeDao).deleteById(5L);
    }

    @Test
    @DisplayName("deleteTrainee: unhappy path throws when trainee not found")
    void deleteTrainee_unhappyPath_notFound() {
        when(traineeDao.findById(7L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.deleteTrainee(7L)
        );

        assertEquals("Trainee not found: id=7", ex.getMessage());
        verify(traineeDao).findById(7L);
        verify(traineeDao, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("selectTrainee: happy path returns trainee")
    void selectTrainee_happyPath() {
        Trainee existing = new Trainee();
        existing.setUserId(3L);

        when(traineeDao.findById(3L)).thenReturn(Optional.of(existing));

        Optional<Trainee> result = traineeService.selectTrainee(3L);

        assertTrue(result.isPresent());
        assertEquals(3L, result.get().getUserId());
        verify(traineeDao).findById(3L);
    }

    @Test
    @DisplayName("selectTrainee: unhappy path returns empty when not found")
    void selectTrainee_unhappyPath_notFound() {
        when(traineeDao.findById(8L)).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.selectTrainee(8L);

        assertFalse(result.isPresent());
        verify(traineeDao).findById(8L);
    }

    @Test
    @DisplayName("selectTraineeByUsername delegates to DAO")
    void selectTraineeByUsername() {
        Trainee existing = new Trainee();
        existing.setUsername("john.smith");

        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(existing));

        Optional<Trainee> result = traineeService.selectTraineeByUsername("john.smith");

        assertTrue(result.isPresent());
        assertEquals("john.smith", result.get().getUsername());
        verify(traineeDao).findByUsername("john.smith");
    }

    @Test
    @DisplayName("selectAllTrainees delegates to DAO and returns list")
    void selectAllTrainees() {
        Trainee t1 = new Trainee();
        Trainee t2 = new Trainee();

        when(traineeDao.findAll()).thenReturn(List.of(t1, t2));

        List<Trainee> result = traineeService.selectAllTrainees();

        assertEquals(2, result.size());
        verify(traineeDao).findAll();
    }
}