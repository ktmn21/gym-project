package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.dao.UserDao;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.User;
import com.example.gymcrm.service.AuthenticationService;
import com.example.gymcrm.util.UsernamePasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private UserDao userDao;
    @Mock private UsernamePasswordGenerator generator;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks
    private TraineeServiceImpl service;

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "secret123";

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

    private Trainee buildTrainee(boolean active) {
        Trainee trainee = new Trainee();
        trainee.setUser(buildUser(active));
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("Street 1");
        trainee.setTrainers(new HashSet<>());
        return trainee;
    }

    private Trainer buildTrainer(Long id) {
        Trainer trainer = new Trainer();
        trainer.setId(id);
        trainer.setTrainees(new HashSet<>());
        return trainer;
    }

    // =====================================================================
    // createProfile
    // =====================================================================
    @Nested
    @DisplayName("createProfile")
    class CreateProfile {

        @Test
        @DisplayName("HAPPY: creates trainee with generated username & password")
        void createProfile_success() {
            when(generator.generateUsername(eq("John"), eq("Doe"), any()))
                    .thenReturn(USERNAME);
            when(generator.generatePassword()).thenReturn(PASSWORD);
            when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.createProfile("John", "Doe",
                    LocalDate.of(1990, 1, 1), "Street 1");

            assertNotNull(result);
            assertEquals(USERNAME, result.getUser().getUsername());
            assertEquals(PASSWORD, result.getUser().getPassword());
            assertTrue(result.getUser().isActive());
            assertEquals("Street 1", result.getAddress());
            verify(traineeDao).save(any(Trainee.class));
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is blank")
        void createProfile_blankFirstName() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.createProfile("  ", "Doe", null, null));
            assertTrue(ex.getMessage().contains("firstName"));
            verifyNoInteractions(traineeDao);
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is null")
        void createProfile_nullFirstName() {
            assertThrows(ValidationException.class,
                    () -> service.createProfile(null, "Doe", null, null));
            verifyNoInteractions(traineeDao);
        }

        @Test
        @DisplayName("UNHAPPY: throws when lastName is blank")
        void createProfile_blankLastName() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.createProfile("John", "", null, null));
            assertTrue(ex.getMessage().contains("lastName"));
            verifyNoInteractions(traineeDao);
        }
    }

    // =====================================================================
    // selectByUsername
    // =====================================================================
    @Nested
    @DisplayName("selectByUsername")
    class SelectByUsername {

        @Test
        @DisplayName("HAPPY: returns trainee after authentication")
        void select_success() {
            Trainee trainee = buildTrainee(true);
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            Trainee result = service.selectByUsername(USERNAME, PASSWORD);

            assertSame(trainee, result);
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void select_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.selectByUsername(USERNAME, PASSWORD));
            verify(traineeDao, never()).findByUserName(anyString());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void select_notFound() {
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.empty());

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
        @DisplayName("HAPPY: updates fields and returns updated trainee")
        void update_success() {
            Trainee trainee = buildTrainee(true);
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.updateProfile(USERNAME, PASSWORD,
                    "Jane", "Smith", LocalDate.of(1985, 5, 5), "New Address");

            assertEquals("Jane", result.getUser().getFirstName());
            assertEquals("Smith", result.getUser().getLastName());
            assertEquals(LocalDate.of(1985, 5, 5), result.getDateOfBirth());
            assertEquals("New Address", result.getAddress());
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
            verify(traineeDao).update(trainee);
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is blank")
        void update_blankFirstName() {
            assertThrows(ValidationException.class,
                    () -> service.updateProfile(USERNAME, PASSWORD, "", "Smith", null, null));
            verify(traineeDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void update_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.updateProfile(USERNAME, PASSWORD, "Jane", "Smith", null, null));
            verify(traineeDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void update_notFound() {
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateProfile(USERNAME, PASSWORD, "Jane", "Smith", null, null));
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
            Trainee trainee = buildTrainee(true);
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.changePassword(USERNAME, PASSWORD, "newPass123");

            assertEquals("newPass123", trainee.getUser().getPassword());
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
            verify(traineeDao).update(trainee);
        }

        @Test
        @DisplayName("UNHAPPY: throws when new password is blank")
        void changePassword_blankNew() {
            assertThrows(ValidationException.class,
                    () -> service.changePassword(USERNAME, PASSWORD, "  "));
            verify(traineeDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void changePassword_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.changePassword(USERNAME, PASSWORD, "newPass123"));
            verify(traineeDao, never()).update(any());
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
            Trainee trainee = buildTrainee(true);
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.toggleActive(USERNAME, PASSWORD);

            assertFalse(trainee.getUser().isActive());
            verify(traineeDao).update(trainee);
        }

        @Test
        @DisplayName("HAPPY: inactive -> active")
        void toggle_inactiveToActive() {
            Trainee trainee = buildTrainee(false);
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.toggleActive(USERNAME, PASSWORD);

            assertTrue(trainee.getUser().isActive());
            verify(traineeDao).update(trainee);
        }

        @Test
        @DisplayName("BEHAVIOR: two toggles return to original (non-idempotent)")
        void toggle_twiceReturnsToOriginal() {
            Trainee trainee = buildTrainee(true);
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.toggleActive(USERNAME, PASSWORD);
            assertFalse(trainee.getUser().isActive());

            service.toggleActive(USERNAME, PASSWORD);
            assertTrue(trainee.getUser().isActive());   // back to original

            verify(traineeDao, times(2)).update(trainee);
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void toggle_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.toggleActive(USERNAME, PASSWORD));
            verify(traineeDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void toggle_notFound() {
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.toggleActive(USERNAME, PASSWORD));
        }
    }

    // =====================================================================
    // deleteByUsername
    // =====================================================================
    @Nested
    @DisplayName("deleteByUsername")
    class DeleteByUsername {

        @Test
        @DisplayName("HAPPY: deletes trainee")
        void delete_success() {
            Trainee trainee = buildTrainee(true);
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.deleteByUsername(USERNAME, PASSWORD);

            verify(authenticationService).authenticate(USERNAME, PASSWORD);
            verify(traineeDao).delete(trainee);
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void delete_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.deleteByUsername(USERNAME, PASSWORD));
            verify(traineeDao, never()).delete(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void delete_notFound() {
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.deleteByUsername(USERNAME, PASSWORD));
            verify(traineeDao, never()).delete(any());
        }
    }

    // =====================================================================
    // getTraineeTrainings
    // =====================================================================
    @Nested
    @DisplayName("getTraineeTrainings")
    class GetTraineeTrainings {

        @Test
        @DisplayName("HAPPY: returns trainings from dao")
        void getTrainings_success() {
            List<Training> trainings = List.of(new Training(), new Training());
            when(trainingDao.findTraineeTrainings(USERNAME, null, null, null, null))
                    .thenReturn(trainings);

            List<Training> result = service.getTraineeTrainings(
                    USERNAME, PASSWORD, null, null, null, null);

            assertEquals(2, result.size());
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("HAPPY: passes all criteria to dao")
        void getTrainings_withCriteria() {
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 12, 31);
            when(trainingDao.findTraineeTrainings(USERNAME, from, to, "Trainer", "Cardio"))
                    .thenReturn(Collections.emptyList());

            List<Training> result = service.getTraineeTrainings(
                    USERNAME, PASSWORD, from, to, "Trainer", "Cardio");

            assertTrue(result.isEmpty());
            verify(trainingDao).findTraineeTrainings(USERNAME, from, to, "Trainer", "Cardio");
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void getTrainings_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.getTraineeTrainings(USERNAME, PASSWORD, null, null, null, null));
            verifyNoInteractions(trainingDao);
        }
    }

    // =====================================================================
    // getTrainersNotAssigned
    // =====================================================================
    @Nested
    @DisplayName("getTrainersNotAssigned")
    class GetTrainersNotAssigned {

        @Test
        @DisplayName("HAPPY: returns unassigned trainers")
        void notAssigned_success() {
            List<Trainer> trainers = List.of(buildTrainer(1L), buildTrainer(2L));
            when(trainerDao.findAllNotAssignedToTrainee(USERNAME)).thenReturn(trainers);

            List<Trainer> result = service.getTrainersNotAssigned(USERNAME, PASSWORD);

            assertEquals(2, result.size());
            verify(authenticationService).authenticate(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void notAssigned_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.getTrainersNotAssigned(USERNAME, PASSWORD));
            verifyNoInteractions(trainerDao);
        }
    }

    // =====================================================================
    // updateTrainersList
    // =====================================================================
    @Nested
    @DisplayName("updateTrainersList")
    class UpdateTrainersList {

        @Test
        @DisplayName("HAPPY: assigns new trainers and maintains both sides")
        void updateTrainers_success() {
            Trainee trainee = buildTrainee(true);
            Trainer t1 = buildTrainer(1L);
            Trainer t2 = buildTrainer(2L);

            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerDao.findAllNotAssignedToTrainee(USERNAME))
                    .thenReturn(List.of(t1, t2));
            when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.updateTrainersList(
                    USERNAME, PASSWORD, Set.of(1L, 2L));

            assertEquals(2, result.getTrainers().size());
            assertTrue(result.getTrainers().contains(t1));
            assertTrue(result.getTrainers().contains(t2));
            // both sides maintained
            assertTrue(t1.getTrainees().contains(trainee));
            assertTrue(t2.getTrainees().contains(trainee));
            verify(traineeDao).update(trainee);
        }

        @Test
        @DisplayName("HAPPY: keeps an already-assigned trainer")
        void updateTrainers_keepExisting() {
            Trainee trainee = buildTrainee(true);
            Trainer existing = buildTrainer(1L);
            trainee.getTrainers().add(existing);
            existing.getTrainees().add(trainee);

            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerDao.findAllNotAssignedToTrainee(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.updateTrainersList(
                    USERNAME, PASSWORD, Set.of(1L));

            assertEquals(1, result.getTrainers().size());
            assertTrue(result.getTrainers().contains(existing));
        }

        @Test
        @DisplayName("HAPPY: empty set removes all trainers")
        void updateTrainers_emptyClearsAll() {
            Trainee trainee = buildTrainee(true);
            Trainer existing = buildTrainer(1L);
            trainee.getTrainers().add(existing);
            existing.getTrainees().add(trainee);

            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerDao.findAllNotAssignedToTrainee(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.updateTrainersList(
                    USERNAME, PASSWORD, Collections.emptySet());

            assertTrue(result.getTrainers().isEmpty());
            assertFalse(existing.getTrainees().contains(trainee)); // detached both sides
        }

        @Test
        @DisplayName("UNHAPPY: throws when a trainer id is not allowed/found")
        void updateTrainers_invalidId() {
            Trainee trainee = buildTrainee(true);
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerDao.findAllNotAssignedToTrainee(USERNAME))
                    .thenReturn(Collections.emptyList());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> service.updateTrainersList(USERNAME, PASSWORD, Set.of(999L)));
            assertTrue(ex.getMessage().contains("999"));
            verify(traineeDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when authentication fails")
        void updateTrainers_authFails() {
            doThrow(new ValidationException("Bad credentials"))
                    .when(authenticationService).authenticate(USERNAME, PASSWORD);

            assertThrows(ValidationException.class,
                    () -> service.updateTrainersList(USERNAME, PASSWORD, Set.of(1L)));
            verify(traineeDao, never()).update(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void updateTrainers_traineeNotFound() {
            when(traineeDao.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateTrainersList(USERNAME, PASSWORD, Set.of(1L)));
        }
    }
}