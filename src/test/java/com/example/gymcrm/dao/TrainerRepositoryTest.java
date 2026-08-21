package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        em.getEntityManager().createQuery("delete from Training").executeUpdate();
        em.getEntityManager().createQuery("delete from Trainer").executeUpdate();
        em.getEntityManager().createQuery("delete from Trainee").executeUpdate();
        em.getEntityManager().createQuery("delete from User").executeUpdate();
        em.getEntityManager().createQuery("delete from TrainingType").executeUpdate();

        for (String name : List.of("Cardio", "Strength", "Yoga")) {
            TrainingType type = new TrainingType();
            type.setTrainingTypeName(name);
            em.persist(type);
        }
        em.flush();
    }

    private TrainingType getExistingTrainingType(String name) {
        List<TrainingType> types = em.getEntityManager().createQuery(
                        "SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name",
                        TrainingType.class)
                .setParameter("name", name)
                .getResultList();

        assertFalse(types.isEmpty(), "TrainingType '" + name + "' must exist");
        return types.get(0);
    }

    @Test
    @DisplayName("save() should persist trainer")
    void saveShouldPersistTrainer() {
        TrainingType type = getExistingTrainingType("Cardio");

        User user = new User();
        user.setFirstName("Alice");
        user.setLastName("Trainer");
        user.setUsername("alice.trainer");
        user.setPassword("pwd");
        user.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(type);

        Trainer saved = trainerRepository.save(trainer);

        em.flush();
        em.clear();

        assertNotNull(saved.getId());

        Optional<Trainer> found = trainerRepository.findByUsername("alice.trainer");
        assertTrue(found.isPresent());
        assertEquals("alice.trainer", found.get().getUser().getUsername());
        assertEquals("Alice", found.get().getUser().getFirstName());
        assertEquals("Cardio", found.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findByUsername() should return trainer when exists")
    void findByUsernameShouldReturnTrainer() {
        TrainingType type = getExistingTrainingType("Strength");

        User user = new User();
        user.setFirstName("Bob");
        user.setLastName("Coach");
        user.setUsername("bob.coach");
        user.setPassword("pwd");
        user.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(type);

        trainerRepository.save(trainer);
        em.flush();
        em.clear();

        Optional<Trainer> found = trainerRepository.findByUsername("bob.coach");

        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getUser().getFirstName());
        assertEquals("Coach", found.get().getUser().getLastName());
        assertEquals("Strength", found.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findByUsername() should return empty when trainer does not exist")
    void findByUsernameShouldReturnEmpty() {
        assertTrue(trainerRepository.findByUsername("missing.trainer").isEmpty());
    }

    @Test
    @DisplayName("save() should merge trainer changes")
    void updateShouldMergeTrainerChanges() {
        TrainingType type = getExistingTrainingType("Yoga");

        User user = new User();
        user.setFirstName("Carol");
        user.setLastName("Fit");
        user.setUsername("carol.fit");
        user.setPassword("pwd");
        user.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(type);

        trainerRepository.save(trainer);
        em.flush();
        em.clear();

        Optional<Trainer> found = trainerRepository.findByUsername("carol.fit");
        assertTrue(found.isPresent());

        Trainer toUpdate = found.get();
        toUpdate.getUser().setFirstName("Caroline");

        Trainer updated = trainerRepository.save(toUpdate);   // ← was update()
        em.flush();
        em.clear();

        Optional<Trainer> reloaded = trainerRepository.findByUsername("carol.fit");

        assertNotNull(updated);
        assertTrue(reloaded.isPresent());
        assertEquals("Caroline", reloaded.get().getUser().getFirstName());
        assertEquals("Yoga", reloaded.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findAllNotAssignedToTrainee() should return only unassigned trainers")
    void findAllNotAssignedToTraineeShouldReturnUnassignedTrainers() {
        TrainingType cardio = getExistingTrainingType("Cardio");
        TrainingType strength = getExistingTrainingType("Strength");

        User traineeUser = new User();
        traineeUser.setFirstName("Trainee");
        traineeUser.setLastName("One");
        traineeUser.setUsername("trainee.one");
        traineeUser.setPassword("pwd");
        traineeUser.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        trainee.setAddress("Bishkek");
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));

        User assignedUser = new User();
        assignedUser.setFirstName("Assigned");
        assignedUser.setLastName("Trainer");
        assignedUser.setUsername("assigned.trainer");
        assignedUser.setPassword("pwd");
        assignedUser.setActive(true);

        Trainer assignedTrainer = new Trainer();
        assignedTrainer.setUser(assignedUser);
        assignedTrainer.setSpecialization(cardio);

        User freeUser = new User();
        freeUser.setFirstName("Free");
        freeUser.setLastName("Trainer");
        freeUser.setUsername("free.trainer");
        freeUser.setPassword("pwd");
        freeUser.setActive(true);

        Trainer freeTrainer = new Trainer();
        freeTrainer.setUser(freeUser);
        freeTrainer.setSpecialization(strength);

        em.persist(assignedTrainer);
        em.persist(freeTrainer);

        trainee.getTrainers().add(assignedTrainer);
        assignedTrainer.getTrainees().add(trainee);

        em.persist(trainee);
        em.flush();
        em.clear();

        List<Trainer> result = trainerRepository.findAllNotAssignedToTrainee("trainee.one");

        assertEquals(1, result.size());
        assertEquals("free.trainer", result.get(0).getUser().getUsername());
    }
}