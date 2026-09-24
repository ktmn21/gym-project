package com.example.gymcrm.service;

import com.example.gymcrm.dao.TraineeRepository;
import com.example.gymcrm.dao.TrainerRepository;
import com.example.gymcrm.dao.TrainingRepository;
import com.example.gymcrm.dao.UserRepository;
import com.example.gymcrm.dto.trainee.TraineeRegistrationResponse;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.metrics.GymMetrics;
import com.example.gymcrm.model.Role;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.User;
import com.example.gymcrm.service.impl.TraineeServiceImpl;
import com.example.gymcrm.util.UsernamePasswordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private UserRepository userRepository;
    @Mock private UsernamePasswordGenerator generator;
    @Mock private GymMetrics gymMetrics;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TraineeServiceImpl service;

    private static final String USERNAME = "John.Doe";
    private static final String RAW_PASSWORD = "secret123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedvalue";

    private User buildUser(boolean active) {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(USERNAME);
        user.setPassword(HASHED_PASSWORD);
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

    @Nested
    @DisplayName("createProfile")
    class CreateProfile {

        @Test
        @DisplayName("HAPPY: creates trainee, hashes password, assigns role")
        void createProfile_success() {
            when(generator.generateUsername(eq("John"), eq("Doe"), any())).thenReturn(USERNAME);
            when(generator.generatePassword()).thenReturn(RAW_PASSWORD);
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            TraineeRegistrationResponse result = service.createProfile("John", "Doe",
                    LocalDate.of(1990, 1, 1), "Street 1");

            assertNotNull(result);
            assertEquals(USERNAME, result.getUsername());
            assertEquals(RAW_PASSWORD, result.getPassword());
            verify(traineeRepository).save(any(Trainee.class));
            verify(gymMetrics).incrementTraineeCreated();
            verify(passwordEncoder).encode(RAW_PASSWORD);
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is blank")
        void createProfile_blankFirstName() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.createProfile("  ", "Doe", null, null));
            assertTrue(ex.getMessage().contains("firstName"));
            verifyNoInteractions(traineeRepository);
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is null")
        void createProfile_nullFirstName() {
            assertThrows(ValidationException.class,
                    () -> service.createProfile(null, "Doe", null, null));
            verifyNoInteractions(traineeRepository);
        }

        @Test
        @DisplayName("UNHAPPY: throws when lastName is blank")
        void createProfile_blankLastName() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.createProfile("John", "", null, null));
            assertTrue(ex.getMessage().contains("lastName"));
            verifyNoInteractions(traineeRepository);
        }
    }

    @Nested
    @DisplayName("selectByUsername")
    class SelectByUsername {

        @Test
        @DisplayName("HAPPY: returns trainee")
        void select_success() {
            Trainee trainee = buildTrainee(true);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            Trainee result = service.selectByUsername(USERNAME);

            assertSame(trainee, result);
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void select_notFound() {
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.selectByUsername(USERNAME));
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("HAPPY: updates fields and returns updated trainee")
        void update_success() {
            Trainee trainee = buildTrainee(true);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.updateProfile(USERNAME,
                    "Jane", "Smith", LocalDate.of(1985, 5, 5), "New Address");

            assertEquals("Jane", result.getUser().getFirstName());
            assertEquals("Smith", result.getUser().getLastName());
            assertEquals(LocalDate.of(1985, 5, 5), result.getDateOfBirth());
            assertEquals("New Address", result.getAddress());
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is blank")
        void update_blankFirstName() {
            assertThrows(ValidationException.class,
                    () -> service.updateProfile(USERNAME, "", "Smith", null, null));
            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void update_notFound() {
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateProfile(USERNAME, "Jane", "Smith", null, null));
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("HAPPY: changes password (verifies old, hashes new)")
        void changePassword_success() {
            Trainee trainee = buildTrainee(true);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
            when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$newhash");

            service.changePassword(USERNAME, RAW_PASSWORD, "newPass123");

            assertEquals("$2a$10$newhash", trainee.getUser().getPassword());
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("UNHAPPY: throws when new password is blank")
        void changePassword_blankNew() {
            assertThrows(ValidationException.class,
                    () -> service.changePassword(USERNAME, RAW_PASSWORD, "  "));
            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when old password is incorrect")
        void changePassword_wrongOld() {
            Trainee trainee = buildTrainee(true);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(passwordEncoder.matches("wrongOld", HASHED_PASSWORD)).thenReturn(false);

            assertThrows(RuntimeException.class,
                    () -> service.changePassword(USERNAME, "wrongOld", "newPass123"));
            verify(traineeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("toggleActive")
    class ToggleActive {

        @Test
        @DisplayName("HAPPY: active -> inactive")
        void toggle_activeToInactive() {
            Trainee trainee = buildTrainee(true);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.toggleActive(USERNAME);

            assertFalse(trainee.getUser().isActive());
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("HAPPY: inactive -> active")
        void toggle_inactiveToActive() {
            Trainee trainee = buildTrainee(false);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.toggleActive(USERNAME);

            assertTrue(trainee.getUser().isActive());
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("BEHAVIOR: two toggles return to original")
        void toggle_twiceReturnsToOriginal() {
            Trainee trainee = buildTrainee(true);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.toggleActive(USERNAME);
            assertFalse(trainee.getUser().isActive());

            service.toggleActive(USERNAME);
            assertTrue(trainee.getUser().isActive());

            verify(traineeRepository, times(2)).save(trainee);
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void toggle_notFound() {
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.toggleActive(USERNAME));
        }
    }

    @Nested
    @DisplayName("deleteByUsername")
    class DeleteByUsername {

        @Test
        @DisplayName("HAPPY: deletes trainee")
        void delete_success() {
            Trainee trainee = buildTrainee(true);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            service.deleteByUsername(USERNAME);

            verify(traineeRepository).delete(trainee);
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void delete_notFound() {
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.deleteByUsername(USERNAME));
            verify(traineeRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getTraineeTrainings")
    class GetTraineeTrainings {

        @Test
        @DisplayName("HAPPY: returns trainings from repository")
        void getTrainings_success() {
            List<Training> trainings = List.of(new Training(), new Training());
            when(trainingRepository.findTraineeTrainings(USERNAME, null, null, null, null))
                    .thenReturn(trainings);

            List<Training> result = service.getTraineeTrainings(
                    USERNAME, null, null, null, null);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("HAPPY: passes all criteria to repository")
        void getTrainings_withCriteria() {
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 12, 31);
            when(trainingRepository.findTraineeTrainings(USERNAME, from, to, "Trainer", "Cardio"))
                    .thenReturn(Collections.emptyList());

            List<Training> result = service.getTraineeTrainings(
                    USERNAME, from, to, "Trainer", "Cardio");

            assertTrue(result.isEmpty());
            verify(trainingRepository).findTraineeTrainings(USERNAME, from, to, "Trainer", "Cardio");
        }
    }

    @Nested
    @DisplayName("getTrainersNotAssigned")
    class GetTrainersNotAssigned {

        @Test
        @DisplayName("HAPPY: returns unassigned trainers")
        void notAssigned_success() {
            List<Trainer> trainers = List.of(buildTrainer(1L), buildTrainer(2L));
            when(trainerRepository.findAllNotAssignedToTrainee(USERNAME)).thenReturn(trainers);

            List<Trainer> result = service.getTrainersNotAssigned(USERNAME);

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("updateTrainersList")
    class UpdateTrainersList {

        @Test
        @DisplayName("HAPPY: assigns new trainers and maintains both sides")
        void updateTrainers_success() {
            Trainee trainee = buildTrainee(true);
            Trainer t1 = buildTrainer(1L);
            Trainer t2 = buildTrainer(2L);

            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerRepository.findAllNotAssignedToTrainee(USERNAME))
                    .thenReturn(List.of(t1, t2));
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.updateTrainersList(USERNAME, Set.of(1L, 2L));

            assertEquals(2, result.getTrainers().size());
            assertTrue(result.getTrainers().contains(t1));
            assertTrue(result.getTrainers().contains(t2));
            assertTrue(t1.getTrainees().contains(trainee));
            assertTrue(t2.getTrainees().contains(trainee));
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("HAPPY: keeps an already-assigned trainer")
        void updateTrainers_keepExisting() {
            Trainee trainee = buildTrainee(true);
            Trainer existing = buildTrainer(1L);
            trainee.getTrainers().add(existing);
            existing.getTrainees().add(trainee);

            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerRepository.findAllNotAssignedToTrainee(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.updateTrainersList(USERNAME, Set.of(1L));

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

            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerRepository.findAllNotAssignedToTrainee(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainee result = service.updateTrainersList(USERNAME, Collections.emptySet());

            assertTrue(result.getTrainers().isEmpty());
            assertFalse(existing.getTrainees().contains(trainee));
        }

        @Test
        @DisplayName("UNHAPPY: throws when a trainer id is not allowed/found")
        void updateTrainers_invalidId() {
            Trainee trainee = buildTrainee(true);
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerRepository.findAllNotAssignedToTrainee(USERNAME))
                    .thenReturn(Collections.emptyList());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> service.updateTrainersList(USERNAME, Set.of(999L)));
            assertTrue(ex.getMessage().contains("999"));
            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainee not found")
        void updateTrainers_traineeNotFound() {
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateTrainersList(USERNAME, Set.of(1L)));
        }
    }
}