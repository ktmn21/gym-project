package com.example.gymcrm.service;

import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.dao.TrainingTypeDao;
import com.example.gymcrm.dao.UserDao;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.model.User;
import com.example.gymcrm.service.AuthenticationService;
import com.example.gymcrm.service.impl.TrainerServiceImpl;
import com.example.gymcrm.util.UsernamePasswordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private TrainingTypeDao trainingTypeDao;
    @Mock private UserDao userDao;
    @Mock private UsernamePasswordGenerator generator;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks
    private TrainerServiceImpl service;

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "secret123";
    private static final Long SPEC_ID = 1L;

    // ---------- helpers ----------
    private User buildUser(boolean active) {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setActive(active);
        return user;
    }

    private TrainingType buildTrainingType(Long id, String name) {
        TrainingType type = new TrainingType();
        type.setId(id);
        type.setTrainingTypeName(name);   // adjust to your actual setter name
        return type;
    }

    private Trainer buildTrainer(boolean active) {
        Trainer trainer = new Trainer();
        trainer.setUser(buildUser(active));
        trainer.setSpecialization(buildTrainingType(SPEC_ID, "Cardio"));
        return trainer;
    }

    // =====================================================================
    // createProfile
    // =====================================================================
    @Nested
    @DisplayName("createProfile")
    class CreateProfile {

        @Test
        @DisplayName("HAPPY: creates trainer with generated credentials and specialization")
        void create_success() {
            TrainingType cardio = buildTrainingType(SPEC_ID, "Cardio");
            when(generator.generateUsername(eq("John"), eq("Doe"), any())).thenReturn(USERNAME);
            when(generator.generatePassword()).thenReturn(PASSWORD);
            when(trainingTypeDao.findAll()).thenReturn(List.of(cardio));
            when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainer result = service.createProfile("John", "Doe", SPEC_ID);

            assertNotNull(result);
            assertEquals(USERNAME, result.getUser().getUsername());
            assertEquals(PASSWORD, result.getUser().getPassword());
            assertTrue(result.getUser().isActive());
            assertEquals(cardio, result.getSpecialization());
            verify(trainerDao).save(any(Trainer.class));
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is blank")
        void create_blankFirstName() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.createProfile("  ", "Doe", SPEC_ID));
            assertTrue(ex.getMessage().contains("firstName"));
            verifyNoInteractions(trainerDao);
        }

        @Test
        @DisplayName("UNHAPPY: throws when lastName is null")
        void create_nullLastName() {
            assertThrows(ValidationException.class,
                    () -> service.createProfile("John", null, SPEC_ID));
            verifyNoInteractions(trainerDao);
        }

        @Test
        @DisplayName("UNHAPPY: throws when specializationId is null")
        void create_nullSpecialization() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.createProfile("John", "Doe", null));
            assertTrue(ex.getMessage().contains("specializationId"));
            verifyNoInteractions(trainerDao);
        }

        @Test
        @DisplayName("UNHAPPY: throws when specialization (TrainingType) not found")
        void create_specializationNotFound() {
            when(generator.generateUsername(anyString(), anyString(), any())).thenReturn(USERNAME);
            when(generator.generatePassword()).thenReturn(PASSWORD);
            when(trainingTypeDao.findAll()).thenReturn(Collections.emptyList());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> service.createProfile("John", "Doe", SPEC_ID));
            assertTrue(ex.getMessage().contains("TrainingType not found"));
            verify(trainerDao, never()).save(any());
        }
    }

    // =====================================================================
    // selectByUsername
    // =====================================================================
    @Nested
    @DisplayName("selectByUsername")
    class SelectByUsername {

        @Test
        @DisplayName("HAPPY: returns trainer after authentication")
        void select_success() {
            Trainer trainer = buildTrainer(true);
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            Trainer result = service.selectByUsername(USERNAME, PASSWORD);

            assertSame(trainer, result);
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void select_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.selectByUsername(USERNAME, PASSWORD));
            verify(trainerDao, never()).findByUsername(anyString());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainer not found")
        void select_notFound() {
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.selectByUsername(USERNAME, PASSWORD));
        }
    }

    // =====================================================================
    // updateProfile
    // =====================================================================
    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("HAPPY: updates name and specialization")
        void update_success() {
            Trainer trainer = buildTrainer(true);
            TrainingType strength = buildTrainingType(2L, "Strength");
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainingTypeDao.findAll()).thenReturn(List.of(strength));
            when(trainerDao.update(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainer result = service.updateProfile(USERNAME, PASSWORD, "Jane", "Smith", 2L);

            assertEquals("Jane", result.getUser().getFirstName());
            assertEquals("Smith", result.getUser().getLastName());
            assertEquals(strength, result.getSpecialization());
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
            verify(trainerDao).update(trainer);
        }

        @Test
        @DisplayName("HAPPY: keeps specialization when specializationId is null")
        void update_nullSpecializationKeepsExisting() {
            Trainer trainer = buildTrainer(true);
            TrainingType original = trainer.getSpecialization();
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainerDao.update(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainer result = service.updateProfile(USERNAME, PASSWORD, "Jane", "Smith", null);

            assertEquals("Jane", result.getUser().getFirstName());
            assertSame(original, result.getSpecialization());   // unchanged
            verify(trainingTypeDao, never()).findAll();          // not looked up
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is blank")
        void update_blankFirstName() {
            assertThrows(ValidationException.class,
                    () -> service.updateProfile(USERNAME, PASSWORD, "", "Smith", SPEC_ID));
            verify(trainerDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void update_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.updateProfile(USERNAME, PASSWORD, "Jane", "Smith", SPEC_ID));
            verify(trainerDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainer not found")
        void update_notFound() {
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateProfile(USERNAME, PASSWORD, "Jane", "Smith", SPEC_ID));
        }

        @Test
        @DisplayName("UNHAPPY: throws when new specialization not found")
        void update_specializationNotFound() {
            Trainer trainer = buildTrainer(true);
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainingTypeDao.findAll()).thenReturn(Collections.emptyList());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateProfile(USERNAME, PASSWORD, "Jane", "Smith", 99L));
            verify(trainerDao, never()).update(any());
        }
    }

    // =====================================================================
    // changePassword
    // =====================================================================
    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("HAPPY: changes password")
        void changePassword_success() {
            Trainer trainer = buildTrainer(true);
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            service.changePassword(USERNAME, PASSWORD, "newPass123");

            assertEquals("newPass123", trainer.getUser().getPassword());
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
            verify(trainerDao).update(trainer);
        }

        @Test
        @DisplayName("UNHAPPY: throws when new password is blank")
        void changePassword_blankNew() {
            assertThrows(ValidationException.class,
                    () -> service.changePassword(USERNAME, PASSWORD, "   "));
            verify(trainerDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void changePassword_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.changePassword(USERNAME, PASSWORD, "newPass123"));
            verify(trainerDao, never()).update(any());
        }
    }

    // =====================================================================
    // toggleActive
    // =====================================================================
    @Nested
    @DisplayName("toggleActive")
    class ToggleActive {

        @Test
        @DisplayName("HAPPY: active -> inactive")
        void toggle_activeToInactive() {
            Trainer trainer = buildTrainer(true);
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            service.toggleActive(USERNAME, PASSWORD);

            assertFalse(trainer.getUser().isActive());
            verify(trainerDao).update(trainer);
        }

        @Test
        @DisplayName("HAPPY: inactive -> active")
        void toggle_inactiveToActive() {
            Trainer trainer = buildTrainer(false);
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            service.toggleActive(USERNAME, PASSWORD);

            assertTrue(trainer.getUser().isActive());
            verify(trainerDao).update(trainer);
        }

        @Test
        @DisplayName("BEHAVIOR: two toggles return to original (non-idempotent)")
        void toggle_twiceReturnsToOriginal() {
            Trainer trainer = buildTrainer(true);
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            service.toggleActive(USERNAME, PASSWORD);
            assertFalse(trainer.getUser().isActive());

            service.toggleActive(USERNAME, PASSWORD);
            assertTrue(trainer.getUser().isActive());

            verify(trainerDao, times(2)).update(trainer);
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void toggle_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.toggleActive(USERNAME, PASSWORD));
            verify(trainerDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainer not found")
        void toggle_notFound() {
            when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.toggleActive(USERNAME, PASSWORD));
        }
    }

    // =====================================================================
    // getTrainerTrainings
    // =====================================================================
    @Nested
    @DisplayName("getTrainerTrainings")
    class GetTrainerTrainings {

        @Test
        @DisplayName("HAPPY: returns trainings from dao")
        void getTrainings_success() {
            List<Training> trainings = List.of(new Training(), new Training());
            when(trainingDao.findTrainerTrainings(USERNAME, null, null, null))
                    .thenReturn(trainings);

            List<Training> result = service.getTrainerTrainings(
                    USERNAME, PASSWORD, null, null, null);

            assertEquals(2, result.size());
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("HAPPY: passes all criteria to dao")
        void getTrainings_withCriteria() {
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 12, 31);
            when(trainingDao.findTrainerTrainings(USERNAME, from, to, "Trainee Name"))
                    .thenReturn(Collections.emptyList());

            List<Training> result = service.getTrainerTrainings(
                    USERNAME, PASSWORD, from, to, "Trainee Name");

            assertTrue(result.isEmpty());
            verify(trainingDao).findTrainerTrainings(USERNAME, from, to, "Trainee Name");
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void getTrainings_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.getTrainerTrainings(USERNAME, PASSWORD, null, null, null));
            verifyNoInteractions(trainingDao);
        }
    }
}