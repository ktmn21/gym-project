package com.example.gymcrm.service;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.dao.TrainingTypeDao;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private TrainingTypeDao trainingTypeDao;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks
    private TrainingServiceImpl service;

    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINEE_PASSWORD = "secret123";
    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String TRAINING_NAME    = "Morning Cardio";
    private static final String TRAINING_TYPE     = "Cardio";
    private static final LocalDate TRAINING_DATE  = LocalDate.of(2024, 6, 1);
    private static final Integer DURATION         = 60;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainer = new Trainer();
        trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TRAINING_TYPE);   // adjust to your setter name
    }

    // ---------- helper to stub the happy-path lookups ----------
    private void stubAllFound() {
        when(traineeDao.findByUserName(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName(TRAINING_TYPE)).thenReturn(Optional.of(trainingType));
    }

    // ---------- convenience call with defaults ----------
    private Training callAddTraining() {
        return service.addTraining(TRAINEE_USERNAME, TRAINEE_PASSWORD, TRAINER_USERNAME,
                TRAINING_NAME, TRAINING_TYPE, TRAINING_DATE, DURATION);
    }

    // =====================================================================
    // HAPPY PATH
    // =====================================================================
    @Nested
    @DisplayName("addTraining - happy path")
    class HappyPath {

        @Test
        @DisplayName("HAPPY: creates and saves training with all fields set")
        void addTraining_success() {
            stubAllFound();
            when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            Training result = callAddTraining();

            assertNotNull(result);
            assertSame(trainee, result.getTrainee());
            assertSame(trainer, result.getTrainer());
            assertSame(trainingType, result.getTrainingType());
            assertEquals(TRAINING_NAME, result.getTrainingName());
            assertEquals(TRAINING_DATE, result.getTrainingDate());
            assertEquals(DURATION, result.getTrainingDuration());

            verify(authenticationService).authenticate(TRAINEE_USERNAME, TRAINEE_PASSWORD);
            verify(trainingDao).save(any(Training.class));
        }

        @Test
        @DisplayName("HAPPY: passes a fully-populated Training to the dao")
        void addTraining_savesCorrectObject() {
            stubAllFound();
            when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            callAddTraining();

            ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
            verify(trainingDao).save(captor.capture());
            Training saved = captor.getValue();

            assertSame(trainee, saved.getTrainee());
            assertSame(trainer, saved.getTrainer());
            assertEquals(TRAINING_NAME, saved.getTrainingName());
            assertEquals(DURATION, saved.getTrainingDuration());
        }

        @Test
        @DisplayName("HAPPY: authenticate is called BEFORE any dao interaction")
        void addTraining_authOrder() {
            stubAllFound();
            when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            callAddTraining();

            InOrder order = inOrder(authenticationService, traineeDao, trainingDao);
            order.verify(authenticationService).authenticate(TRAINEE_USERNAME, TRAINEE_PASSWORD);
            order.verify(traineeDao).findByUserName(TRAINEE_USERNAME);
            order.verify(trainingDao).save(any(Training.class));
        }
    }

    // =====================================================================
    // AUTHENTICATION
    // =====================================================================
    @Nested
    @DisplayName("addTraining - authentication")
    class Authentication {

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails and touches no dao")
        void addTraining_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(TRAINEE_USERNAME, TRAINEE_PASSWORD);

            assertThrows(ValidationException.class, this::callAddTrainingWrapper);

            verifyNoInteractions(traineeDao, trainerDao, trainingTypeDao, trainingDao);
        }

        private void callAddTrainingWrapper() {
            callAddTraining();
        }
    }

    // =====================================================================
    // VALIDATION
    // =====================================================================
    @Nested
    @DisplayName("addTraining - validation")
    class Validation {

        @Test
        @DisplayName("UNHAPPY: throws when trainingName is blank")
        void addTraining_blankName() {
            ValidationException ex = assertThrows(ValidationException.class, () ->
                    service.addTraining(TRAINEE_USERNAME, TRAINEE_PASSWORD, TRAINER_USERNAME,
                            "  ", TRAINING_TYPE, TRAINING_DATE, DURATION));
            assertTrue(ex.getMessage().contains("trainingName"));
            verify(trainingDao, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainingName is null")
        void addTraining_nullName() {
            assertThrows(ValidationException.class, () ->
                    service.addTraining(TRAINEE_USERNAME, TRAINEE_PASSWORD, TRAINER_USERNAME,
                            null, TRAINING_TYPE, TRAINING_DATE, DURATION));
            verify(trainingDao, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainingTypeName is blank")
        void addTraining_blankType() {
            ValidationException ex = assertThrows(ValidationException.class, () ->
                    service.addTraining(TRAINEE_USERNAME, TRAINEE_PASSWORD, TRAINER_USERNAME,
                            TRAINING_NAME, "", TRAINING_DATE, DURATION));
            assertTrue(ex.getMessage().contains("trainingTypeName"));
            verify(trainingDao, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainingDate is null")
        void addTraining_nullDate() {
            ValidationException ex = assertThrows(ValidationException.class, () ->
                    service.addTraining(TRAINEE_USERNAME, TRAINEE_PASSWORD, TRAINER_USERNAME,
                            TRAINING_NAME, TRAINING_TYPE, null, DURATION));
            assertTrue(ex.getMessage().contains("trainingDate"));
            verify(trainingDao, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainingDuration is null")
        void addTraining_nullDuration() {
            ValidationException ex = assertThrows(ValidationException.class, () ->
                    service.addTraining(TRAINEE_USERNAME, TRAINEE_PASSWORD, TRAINER_USERNAME,
                            TRAINING_NAME, TRAINING_TYPE, TRAINING_DATE, null));
            assertTrue(ex.getMessage().contains("trainingDuration"));
            verify(trainingDao, never()).save(any());
        }
    }

    // =====================================================================
    // ENTITY LOOKUP FAILURES
    // =====================================================================
    @Nested
    @DisplayName("addTraining - entity lookups")
    class EntityLookups {

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void addTraining_traineeNotFound() {
            when(traineeDao.findByUserName(TRAINEE_USERNAME)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> callAddTraining());
            assertTrue(ex.getMessage().contains("Trainee not found"));
            verify(trainingDao, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainer not found")
        void addTraining_trainerNotFound() {
            when(traineeDao.findByUserName(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> callAddTraining());
            assertTrue(ex.getMessage().contains("Trainer not found"));
            verify(trainingDao, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when training type not found")
        void addTraining_typeNotFound() {
            when(traineeDao.findByUserName(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
            when(trainingTypeDao.findByName(TRAINING_TYPE)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> callAddTraining());
            assertTrue(ex.getMessage().contains("TrainingType not found"));
            verify(trainingDao, never()).save(any());
        }
    }
}