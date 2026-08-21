package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingDao;

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    void seedTrainingTypes() {
        if (em.createQuery("select count(t) from TrainingType t", Long.class)
                .getSingleResult() == 0) {
            for (String name : List.of("Cardio", "Strength", "Yoga")) {
                TrainingType type = new TrainingType();
                type.setTrainingTypeName(name);
                em.persist(type);
            }
            em.flush();
        }
    }

    private TrainingType getExistingTrainingType(String name) {
        List<TrainingType> types = em.createQuery(
                        "SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name",
                        TrainingType.class)
                .setParameter("name", name)
                .getResultList();

        assertFalse(types.isEmpty(), "TrainingType '" + name + "' must exist in DB for this test");
        return types.get(0);
    }

    private Optional<Trainee> findTraineeByUsername(String username) {
        List<Trainee> result = em.createQuery(
                        "SELECT t FROM Trainee t WHERE t.user.username = :username",
                        Trainee.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst();
    }

    private Optional<Trainer> findTrainerByUsername(String username) {
        List<Trainer> result = em.createQuery(
                        "SELECT t FROM Trainer t WHERE t.user.username = :username",
                        Trainer.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst();
    }

    private Trainee getOrCreateTrainee(String firstName, String lastName, String username) {
        Optional<Trainee> existing = findTraineeByUsername(username);
        if (existing.isPresent()) {
            return existing.get();
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword("pwd");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("Bishkek");
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));

        em.persist(trainee);
        return trainee;
    }

    private Trainer getOrCreateTrainer(String firstName, String lastName, String username, String typeName) {
        Optional<Trainer> existing = findTrainerByUsername(username);
        if (existing.isPresent()) {
            return existing.get();
        }

        TrainingType type = getExistingTrainingType(typeName);

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword("pwd");
        user.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(type);

        em.persist(trainer);
        return trainer;
    }

    private Training createTraining(
            String traineeFirstName,
            String traineeLastName,
            String traineeUsername,
            String trainerFirstName,
            String trainerLastName,
            String trainerUsername,
            String trainingTypeName,
            String trainingName,
            LocalDate trainingDate,
            int duration
    ) {
        Trainee trainee = getOrCreateTrainee(traineeFirstName, traineeLastName, traineeUsername);
        Trainer trainer = getOrCreateTrainer(trainerFirstName, trainerLastName, trainerUsername, trainingTypeName);
        TrainingType type = getExistingTrainingType(trainingTypeName);

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(type);
        training.setTrainingName(trainingName);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(duration);

        return training;
    }

    @Test
    @DisplayName("save() should persist training")
    void saveShouldPersistTraining() {
        Training training = createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "One", "coach.one",
                "Cardio",
                "Morning Cardio",
                LocalDate.of(2026, 7, 1),
                60
        );

        Training saved = trainingDao.save(training);

        em.flush();
        em.clear();

        assertNotNull(saved.getId());

        List<Training> all = em.createQuery("SELECT t FROM Training t", Training.class).getResultList();
        assertEquals(1, all.size());
        assertEquals("Morning Cardio", all.get(0).getTrainingName());
        assertEquals("bob.trainee", all.get(0).getTrainee().getUser().getUsername());
        assertEquals("coach.one", all.get(0).getTrainer().getUser().getUsername());
        assertEquals("Cardio", all.get(0).getTrainingType().getTrainingTypeName());
    }

    @Test
    @DisplayName("findTraineeTrainings() should filter by username only")
    void findTraineeTrainingsShouldFilterByUsernameOnly() {
        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "One", "coach.one",
                "Cardio",
                "Morning Cardio",
                LocalDate.of(2026, 7, 1),
                60
        ));

        trainingDao.save(createTraining(
                "Alice", "Trainee", "alice.trainee",
                "Coach", "Two", "coach.two",
                "Strength",
                "Evening Strength",
                LocalDate.of(2026, 7, 2),
                45
        ));

        em.flush();
        em.clear();

        List<Training> result = trainingDao.findTraineeTrainings(
                "bob.trainee",
                null,
                null,
                null,
                null
        );

        assertEquals(1, result.size());
        assertEquals("Morning Cardio", result.get(0).getTrainingName());
    }

    @Test
    @DisplayName("findTraineeTrainings() should filter by date range and training type")
    void findTraineeTrainingsShouldFilterByDateAndType() {
        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "One", "coach.one",
                "Cardio",
                "Morning Cardio",
                LocalDate.of(2026, 7, 1),
                60
        ));

        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "Two", "coach.two",
                "Strength",
                "Strength Session",
                LocalDate.of(2026, 7, 10),
                50
        ));

        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "Three", "coach.three",
                "Cardio",
                "Late Cardio",
                LocalDate.of(2026, 8, 1),
                40
        ));

        em.flush();
        em.clear();

        List<Training> result = trainingDao.findTraineeTrainings(
                "bob.trainee",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                null,
                "Cardio"
        );

        assertEquals(1, result.size());
        assertEquals("Morning Cardio", result.get(0).getTrainingName());
        assertEquals("Cardio", result.get(0).getTrainingType().getTrainingTypeName());
    }

    @Test
    @DisplayName("findTraineeTrainings() should filter by trainer full name")
    void findTraineeTrainingsShouldFilterByTrainerName() {
        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "One", "coach.one",
                "Cardio",
                "Morning Cardio",
                LocalDate.of(2026, 7, 1),
                60
        ));

        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Anna", "Smith", "anna.smith",
                "Yoga",
                "Yoga Session",
                LocalDate.of(2026, 7, 2),
                45
        ));

        em.flush();
        em.clear();

        List<Training> result = trainingDao.findTraineeTrainings(
                "bob.trainee",
                null,
                null,
                "Coach One",
                null
        );

        assertEquals(1, result.size());
        assertEquals("coach.one", result.get(0).getTrainer().getUser().getUsername());
        assertEquals("Morning Cardio", result.get(0).getTrainingName());
    }

    @Test
    @DisplayName("findTrainerTrainings() should filter by trainer username only")
    void findTrainerTrainingsShouldFilterByUsernameOnly() {
        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "One", "coach.one",
                "Cardio",
                "Morning Cardio",
                LocalDate.of(2026, 7, 1),
                60
        ));

        trainingDao.save(createTraining(
                "Alice", "Trainee", "alice.trainee",
                "Coach", "Two", "coach.two",
                "Strength",
                "Evening Strength",
                LocalDate.of(2026, 7, 2),
                45
        ));

        em.flush();
        em.clear();

        List<Training> result = trainingDao.findTrainerTrainings(
                "coach.one",
                null,
                null,
                null
        );

        assertEquals(1, result.size());
        assertEquals("Morning Cardio", result.get(0).getTrainingName());
        assertEquals("bob.trainee", result.get(0).getTrainee().getUser().getUsername());
    }

    @Test
    @DisplayName("findTrainerTrainings() should filter by date range and trainee full name")
    void findTrainerTrainingsShouldFilterByDateAndTraineeName() {
        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "One", "coach.one",
                "Cardio",
                "Morning Cardio",
                LocalDate.of(2026, 7, 1),
                60
        ));

        trainingDao.save(createTraining(
                "Alice", "Trainee", "alice.trainee",
                "Coach", "One", "coach.one",
                "Yoga",
                "Yoga Session",
                LocalDate.of(2026, 7, 5),
                45
        ));

        trainingDao.save(createTraining(
                "Bob", "Trainee", "bob.trainee",
                "Coach", "One", "coach.one",
                "Strength",
                "Strength Session",
                LocalDate.of(2026, 8, 1),
                50
        ));

        em.flush();
        em.clear();

        List<Training> result = trainingDao.findTrainerTrainings(
                "coach.one",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                "Alice Trainee"
        );

        assertEquals(1, result.size());
        assertEquals("alice.trainee", result.get(0).getTrainee().getUser().getUsername());
        assertEquals("Yoga Session", result.get(0).getTrainingName());
    }
}