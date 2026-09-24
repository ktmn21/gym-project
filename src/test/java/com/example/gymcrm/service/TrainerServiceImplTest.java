package com.example.gymcrm.service;

import com.example.gymcrm.dao.TrainerRepository;
import com.example.gymcrm.dao.TrainingRepository;
import com.example.gymcrm.dao.TrainingTypeRepository;
import com.example.gymcrm.dao.UserRepository;
import com.example.gymcrm.dto.trainer.TrainerRegistrationResponse;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.exceptions.ValidationException;
import com.example.gymcrm.metrics.GymMetrics;
import com.example.gymcrm.model.Role;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.model.User;
import com.example.gymcrm.service.impl.TrainerServiceImpl;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private UserRepository userRepository;
    @Mock private UsernamePasswordGenerator generator;
    @Mock private GymMetrics gymMetrics;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerServiceImpl service;

    private static final String USERNAME = "John.Doe";
    private static final String RAW_PASSWORD = "secret123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedvalue";
    private static final Long SPEC_ID = 1L;

    private User buildUser(boolean active) {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(USERNAME);
        user.setPassword(HASHED_PASSWORD);
        user.setActive(active);
        return user;
    }

    private TrainingType buildTrainingType(Long id, String name) {
        TrainingType type = new TrainingType();
        type.setId(id);
        type.setTrainingTypeName(name);
        return type;
    }

    private Trainer buildTrainer(boolean active) {
        Trainer trainer = new Trainer();
        trainer.setUser(buildUser(active));
        trainer.setSpecialization(buildTrainingType(SPEC_ID, "Cardio"));
        return trainer;
    }

    @Nested
    @DisplayName("createProfile")
    class CreateProfile {

        @Test
        @DisplayName("HAPPY: creates trainer, hashes password, assigns role")
        void create_success() {
            TrainingType cardio = buildTrainingType(SPEC_ID, "Cardio");
            when(generator.generateUsername(eq("John"), eq("Doe"), any())).thenReturn(USERNAME);
            when(generator.generatePassword()).thenReturn(RAW_PASSWORD);
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
            when(trainingTypeRepository.findAll()).thenReturn(List.of(cardio));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            TrainerRegistrationResponse result = service.createProfile("John", "Doe", SPEC_ID);

            assertNotNull(result);
            assertEquals(USERNAME, result.getUsername());
            assertEquals(RAW_PASSWORD, result.getPassword());
            verify(trainerRepository).save(any(Trainer.class));
            verify(gymMetrics).incrementTrainerCreated();
            verify(passwordEncoder).encode(RAW_PASSWORD);
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is blank")
        void create_blankFirstName() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.createProfile("  ", "Doe", SPEC_ID));
            assertTrue(ex.getMessage().contains("firstName"));
            verifyNoInteractions(trainerRepository);
        }

        @Test
        @DisplayName("UNHAPPY: throws when lastName is null")
        void create_nullLastName() {
            assertThrows(ValidationException.class,
                    () -> service.createProfile("John", null, SPEC_ID));
            verifyNoInteractions(trainerRepository);
        }

        @Test
        @DisplayName("UNHAPPY: throws when specializationId is null")
        void create_nullSpecialization() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.createProfile("John", "Doe", null));
            assertTrue(ex.getMessage().contains("specializationId"));
            verifyNoInteractions(trainerRepository);
        }

        @Test
        @DisplayName("UNHAPPY: throws when specialization (TrainingType) not found")
        void create_specializationNotFound() {
            when(generator.generateUsername(anyString(), anyString(), any())).thenReturn(USERNAME);
            when(generator.generatePassword()).thenReturn(RAW_PASSWORD);
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
            when(trainingTypeRepository.findAll()).thenReturn(Collections.emptyList());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> service.createProfile("John", "Doe", SPEC_ID));
            assertTrue(ex.getMessage().contains("TrainingType not found"));
            verify(trainerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("selectByUsername")
    class SelectByUsername {

        @Test
        @DisplayName("HAPPY: returns trainer")
        void select_success() {
            Trainer trainer = buildTrainer(true);
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            Trainer result = service.selectByUsername(USERNAME);

            assertSame(trainer, result);
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainer not found")
        void select_notFound() {
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.selectByUsername(USERNAME));
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("HAPPY: updates name and specialization")
        void update_success() {
            Trainer trainer = buildTrainer(true);
            TrainingType strength = buildTrainingType(2L, "Strength");
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainingTypeRepository.findAll()).thenReturn(List.of(strength));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainer result = service.updateProfile(USERNAME, "Jane", "Smith", 2L);

            assertEquals("Jane", result.getUser().getFirstName());
            assertEquals("Smith", result.getUser().getLastName());
            assertEquals(strength, result.getSpecialization());
            verify(trainerRepository).save(trainer);
        }

        @Test
        @DisplayName("HAPPY: keeps specialization when specializationId is null")
        void update_nullSpecializationKeepsExisting() {
            Trainer trainer = buildTrainer(true);
            TrainingType original = trainer.getSpecialization();
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            Trainer result = service.updateProfile(USERNAME, "Jane", "Smith", null);

            assertEquals("Jane", result.getUser().getFirstName());
            assertSame(original, result.getSpecialization());
            verify(trainingTypeRepository, never()).findAll();
        }

        @Test
        @DisplayName("UNHAPPY: throws when firstName is blank")
        void update_blankFirstName() {
            assertThrows(ValidationException.class,
                    () -> service.updateProfile(USERNAME, "", "Smith", SPEC_ID));
            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainer not found")
        void update_notFound() {
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateProfile(USERNAME, "Jane", "Smith", SPEC_ID));
        }

        @Test
        @DisplayName("UNHAPPY: throws when new specialization not found")
        void update_specializationNotFound() {
            Trainer trainer = buildTrainer(true);
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
            when(trainingTypeRepository.findAll()).thenReturn(Collections.emptyList());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateProfile(USERNAME, "Jane", "Smith", 99L));
            verify(trainerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("HAPPY: changes password (verifies old, hashes new)")
        void changePassword_success() {
            Trainer trainer = buildTrainer(true);
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
            when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
            when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$newhash");

            service.changePassword(USERNAME, RAW_PASSWORD, "newPass123");

            assertEquals("$2a$10$newhash", trainer.getUser().getPassword());
            verify(trainerRepository).save(trainer);
        }

        @Test
        @DisplayName("UNHAPPY: throws when new password is blank")
        void changePassword_blankNew() {
            assertThrows(ValidationException.class,
                    () -> service.changePassword(USERNAME, RAW_PASSWORD, "   "));
            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("UNHAPPY: throws when old password is incorrect")
        void changePassword_wrongOld() {
            Trainer trainer = buildTrainer(true);
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
            when(passwordEncoder.matches("wrongOld", HASHED_PASSWORD)).thenReturn(false);

            assertThrows(RuntimeException.class,
                    () -> service.changePassword(USERNAME, "wrongOld", "newPass123"));
            verify(trainerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("toggleActive")
    class ToggleActive {

        @Test
        @DisplayName("HAPPY: active -> inactive")
        void toggle_activeToInactive() {
            Trainer trainer = buildTrainer(true);
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            service.toggleActive(USERNAME);

            assertFalse(trainer.getUser().isActive());
            verify(trainerRepository).save(trainer);
        }

        @Test
        @DisplayName("HAPPY: inactive -> active")
        void toggle_inactiveToActive() {
            Trainer trainer = buildTrainer(false);
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            service.toggleActive(USERNAME);

            assertTrue(trainer.getUser().isActive());
            verify(trainerRepository).save(trainer);
        }

        @Test
        @DisplayName("BEHAVIOR: two toggles return to original")
        void toggle_twiceReturnsToOriginal() {
            Trainer trainer = buildTrainer(true);
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

            service.toggleActive(USERNAME);
            assertFalse(trainer.getUser().isActive());

            service.toggleActive(USERNAME);
            assertTrue(trainer.getUser().isActive());

            verify(trainerRepository, times(2)).save(trainer);
        }

        @Test
        @DisplayName("UNHAPPY: throws when trainer not found")
        void toggle_notFound() {
            when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.toggleActive(USERNAME));
        }
    }

    @Nested
    @DisplayName("getTrainerTrainings")
    class GetTrainerTrainings {

        @Test
        @DisplayName("HAPPY: returns trainings from repository")
        void getTrainings_success() {
            List<Training> trainings = List.of(new Training(), new Training());
            when(trainingRepository.findTrainerTrainings(USERNAME, null, null, null))
                    .thenReturn(trainings);

            List<Training> result = service.getTrainerTrainings(
                    USERNAME, null, null, null);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("HAPPY: passes all criteria to repository")
        void getTrainings_withCriteria() {
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 12, 31);
            when(trainingRepository.findTrainerTrainings(USERNAME, from, to, "Trainee Name"))
                    .thenReturn(Collections.emptyList());

            List<Training> result = service.getTrainerTrainings(
                    USERNAME, from, to, "Trainee Name");

            assertTrue(result.isEmpty());
            verify(trainingRepository).findTrainerTrainings(USERNAME, from, to, "Trainee Name");
        }
    }
}