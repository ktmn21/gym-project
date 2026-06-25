package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoImplTest {

    private TraineeDaoImpl traineeDao;

    @BeforeEach
    void setUp() {
        traineeDao = new TraineeDaoImpl();
        traineeDao.setInMemoryStorage(new InMemoryStorage());
    }

    @Test
    void save_shouldAssignIdAndStoreTrainee() {
        Trainee trainee = new Trainee(
                null,
                "John",
                "Smith",
                "john.smith",
                "password123",
                true,
                LocalDate.of(2000, 1, 1),
                "Bishkek"
        );

        traineeDao.save(trainee);

        assertNotNull(trainee.getUserId());
        Optional<Trainee> saved = traineeDao.findById(trainee.getUserId());

        assertTrue(saved.isPresent());
        assertEquals("john.smith", saved.get().getUsername());
    }

    @Test
    void findByUsername_shouldReturnMatchingTrainee() {
        Trainee trainee = new Trainee(
                null,
                "Alice",
                "Brown",
                "alice.brown",
                "pass123456",
                true,
                LocalDate.of(1999, 5, 10),
                "Naryn"
        );

        traineeDao.save(trainee);

        Optional<Trainee> found = traineeDao.findByUsername("alice.brown");

        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getFirstName());
        assertEquals("Brown", found.get().getLastName());
    }

    @Test
    void update_shouldReplaceExistingTrainee() {
        Trainee trainee = new Trainee(
                null,
                "Bob",
                "Taylor",
                "bob.taylor",
                "pass123456",
                true,
                LocalDate.of(2001, 3, 15),
                "Kant"
        );

        traineeDao.save(trainee);

        trainee.setAddress("Bishkek");
        trainee.setActive(false);
        traineeDao.update(trainee);

        Optional<Trainee> updated = traineeDao.findById(trainee.getUserId());

        assertTrue(updated.isPresent());
        assertEquals("Bishkek", updated.get().getAddress());
        assertFalse(updated.get().isActive());
    }

    @Test
    void deleteById_shouldRemoveTrainee() {
        Trainee trainee = new Trainee(
                null,
                "Sara",
                "Lee",
                "sara.lee",
                "pass123456",
                true,
                LocalDate.of(2002, 7, 20),
                "Osh"
        );

        traineeDao.save(trainee);
        Long id = trainee.getUserId();

        traineeDao.deleteById(id);

        Optional<Trainee> deleted = traineeDao.findById(id);
        assertFalse(deleted.isPresent());
    }

    @Test
    void findAll_shouldReturnAllStoredTrainees() {
        Trainee trainee1 = new Trainee(
                null,
                "Tom",
                "Hardy",
                "tom.hardy",
                "pass123456",
                true,
                LocalDate.of(1998, 8, 8),
                "Karakol"
        );

        Trainee trainee2 = new Trainee(
                null,
                "Emma",
                "Stone",
                "emma.stone",
                "pass123456",
                true,
                LocalDate.of(1997, 9, 9),
                "Bishkek"
        );

        traineeDao.save(trainee1);
        traineeDao.save(trainee2);

        List<Trainee> all = traineeDao.findAll();

        assertEquals(2, all.size());
    }
}