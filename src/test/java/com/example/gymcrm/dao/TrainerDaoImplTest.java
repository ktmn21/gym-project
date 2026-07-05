package com.example.gymcrm.dao;

import com.example.gymcrm.config.AppConfig;
import com.example.gymcrm.dao.implementations.TrainerDaoImpl;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
@Transactional
class TrainerDaoImplTest {

    @Autowired
    private TrainerDaoImpl trainerDao;

    @PersistenceContext
    private EntityManager em;

    private TrainingType getExistingTrainingType(String name) {
        List<TrainingType> types = em.createQuery(
                        "SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name",
                        TrainingType.class)
                .setParameter("name", name)
                .getResultList();

        assertFalse(types.isEmpty(), "TrainingType '" + name + "' must exist in DB for this test");
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

        Trainer saved = trainerDao.save(trainer);

        em.flush();
        em.clear();

        assertNotNull(saved.getId());

        Optional<Trainer> found = trainerDao.findByUsername("alice.trainer");
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

        trainerDao.save(trainer);

        em.flush();
        em.clear();

        Optional<Trainer> found = trainerDao.findByUsername("bob.coach");

        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getUser().getFirstName());
        assertEquals("Coach", found.get().getUser().getLastName());
        assertEquals("Strength", found.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findByUsername() should return empty when trainer does not exist")
    void findByUsernameShouldReturnEmpty() {
        Optional<Trainer> found = trainerDao.findByUsername("missing.trainer");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("update() should merge trainer changes")
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

        trainerDao.save(trainer);

        em.flush();
        em.clear();

        Optional<Trainer> found = trainerDao.findByUsername("carol.fit");
        assertTrue(found.isPresent());

        Trainer toUpdate = found.get();
        toUpdate.getUser().setFirstName("Caroline");

        Trainer updated = trainerDao.update(toUpdate);

        em.flush();
        em.clear();

        Optional<Trainer> reloaded = trainerDao.findByUsername("carol.fit");

        assertNotNull(updated);
        assertTrue(reloaded.isPresent());
        assertEquals("Caroline", reloaded.get().getUser().getFirstName());
        assertEquals("Yoga", reloaded.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findAllNotAssignedToTrainee() should return only trainers not assigned to trainee")
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

        List<Trainer> result = trainerDao.findAllNotAssignedToTrainee("trainee.one");

        assertEquals(1, result.size());
        assertEquals("free.trainer", result.get(0).getUser().getUsername());
    }
}