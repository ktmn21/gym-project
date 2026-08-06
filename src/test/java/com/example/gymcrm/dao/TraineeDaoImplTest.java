package com.example.gymcrm.dao;

import com.example.gymcrm.dao.implementations.TraineeDaoImpl;
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
@ContextConfiguration(classes = TestPersistenceConfig.class)
@Transactional
class TraineeDaoImplTest {

    @Autowired
    private TraineeDaoImpl traineeDao;

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

    @Test
    @DisplayName("save() should persist trainee")
    void saveShouldPersistTrainee() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("pwd");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("Bishkek");
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));

        Trainee saved = traineeDao.save(trainee);

        em.flush();
        em.clear();

        assertNotNull(saved.getId());

        Optional<Trainee> found = traineeDao.findByUserName("john.doe");
        assertTrue(found.isPresent());
        assertEquals("john.doe", found.get().getUser().getUsername());
        assertEquals("Bishkek", found.get().getAddress());
    }

    @Test
    @DisplayName("findByUserName() should return trainee when exists")
    void findByUserNameShouldReturnTrainee() {
        User user = new User();
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setUsername("alice.smith");
        user.setPassword("pwd");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("Tokmok");
        trainee.setDateOfBirth(LocalDate.of(1999, 5, 10));

        traineeDao.save(trainee);

        em.flush();
        em.clear();

        Optional<Trainee> found = traineeDao.findByUserName("alice.smith");

        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getUser().getFirstName());
        assertEquals("Smith", found.get().getUser().getLastName());
        assertEquals("Tokmok", found.get().getAddress());
    }

    @Test
    @DisplayName("findByUserName() should return empty when trainee does not exist")
    void findByUserNameShouldReturnEmpty() {
        Optional<Trainee> found = traineeDao.findByUserName("missing.user");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("update() should merge trainee changes")
    void updateShouldMergeChanges() {
        User user = new User();
        user.setFirstName("Bob");
        user.setLastName("Brown");
        user.setUsername("bob.brown");
        user.setPassword("pwd");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("Old address");
        trainee.setDateOfBirth(LocalDate.of(2001, 3, 15));

        Trainee saved = traineeDao.save(trainee);

        em.flush();
        em.clear();

        Optional<Trainee> found = traineeDao.findByUserName("bob.brown");
        assertTrue(found.isPresent());

        Trainee toUpdate = found.get();
        toUpdate.setAddress("New address");
        toUpdate.getUser().setFirstName("Bobby");

        Trainee updated = traineeDao.update(toUpdate);

        em.flush();
        em.clear();

        Optional<Trainee> reloaded = traineeDao.findByUserName("bob.brown");

        assertNotNull(updated);
        assertTrue(reloaded.isPresent());
        assertEquals("New address", reloaded.get().getAddress());
        assertEquals("Bobby", reloaded.get().getUser().getFirstName());
    }

    @Test
    @DisplayName("delete() should remove trainee")
    void deleteShouldRemoveTrainee() {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("CARDIO");
        em.persist(type);

        User trainerUser = new User();
        trainerUser.setFirstName("Trainer");
        trainerUser.setLastName("One");
        trainerUser.setUsername("trainer.one");
        trainerUser.setPassword("pwd");
        trainerUser.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(type);
        em.persist(trainer);

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

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(type);
        training.setTrainingName("Morning Cardio");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);

        trainee.getTrainings().add(training);
        trainee.getTrainers().add(trainer);
        trainer.getTrainees().add(trainee);

        Trainee saved = traineeDao.save(trainee);

        em.flush();
        em.clear();

        Optional<Trainee> beforeDelete = traineeDao.findByUserName("trainee.one");
        assertTrue(beforeDelete.isPresent());

        traineeDao.delete(beforeDelete.get());

        em.flush();
        em.clear();

        Optional<Trainee> afterDelete = traineeDao.findByUserName("trainee.one");
        assertTrue(afterDelete.isEmpty());

        List<Trainee> all = traineeDao.findAll();
        assertTrue(all.stream().noneMatch(t -> t.getId().equals(saved.getId())));
    }

    @Test
    @DisplayName("findAll() should return all trainees")
    void findAllShouldReturnAll() {
        User user1 = new User();
        user1.setFirstName("First");
        user1.setLastName("User");
        user1.setUsername("first.user");
        user1.setPassword("pwd");
        user1.setActive(true);

        User user2 = new User();
        user2.setFirstName("Second");
        user2.setLastName("User");
        user2.setUsername("second.user");
        user2.setPassword("pwd");
        user2.setActive(true);

        Trainee trainee1 = new Trainee();
        trainee1.setUser(user1);
        trainee1.setAddress("Addr1");
        trainee1.setDateOfBirth(LocalDate.of(2000, 1, 1));

        Trainee trainee2 = new Trainee();
        trainee2.setUser(user2);
        trainee2.setAddress("Addr2");
        trainee2.setDateOfBirth(LocalDate.of(2001, 2, 2));

        traineeDao.save(trainee1);
        traineeDao.save(trainee2);

        em.flush();
        em.clear();

        List<Trainee> all = traineeDao.findAll();
        System.out.println("Trainees");
        all.forEach(System.out::println);

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(t -> t.getUser().getUsername().equals("first.user")));
        assertTrue(all.stream().anyMatch(t -> t.getUser().getUsername().equals("second.user")));
    }
}