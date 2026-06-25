package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerDaoImpl tests")
class TrainerDaoImplTest {

    @Mock
    private InMemoryStorage inMemoryStorage;

    private TrainerDaoImpl trainerDao;
    private Map<Long, Trainer> backingMap;

    @BeforeEach
    void setUp() {
        backingMap = new HashMap<>();
        when(inMemoryStorage.getTrainerStorage()).thenReturn(backingMap);

        trainerDao = new TrainerDaoImpl();
        trainerDao.setStorage(inMemoryStorage);
    }

    @Test
    @DisplayName("save should assign id and store trainer")
    void save_shouldAssignIdAndStoreTrainer() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setUsername("john.smith");
        trainer.setPassword("pass123456");
        trainer.setActive(true);
        trainer.setSpecialization(1L);

        trainerDao.save(trainer);

        assertThat(trainer.getUserId()).isNotNull();
        assertThat(trainer.getUserId()).isEqualTo(1L);
        assertThat(backingMap).containsKey(1L);
        assertThat(backingMap.get(1L).getUsername()).isEqualTo("john.smith");
    }

    @Test
    @DisplayName("update should replace existing trainer")
    void update_shouldReplaceExistingTrainer() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Alice");
        trainer.setLastName("Brown");
        trainer.setUsername("alice.brown");
        trainer.setPassword("pass123456");
        trainer.setActive(true);
        trainer.setSpecialization(1L);

        trainerDao.save(trainer);

        trainer.setSpecialization(2L);
        trainer.setActive(false);
        trainerDao.update(trainer);

        assertThat(backingMap.get(trainer.getUserId()).getSpecialization()).isEqualTo(2L);
        assertThat(backingMap.get(trainer.getUserId()).isActive()).isFalse();
    }

    @Test
    @DisplayName("findById should return trainer when id exists")
    void findById_shouldReturnTrainerWhenIdExists() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Bob");
        trainer.setLastName("Taylor");
        trainer.setUsername("bob.taylor");
        trainer.setPassword("pass123456");
        trainer.setActive(true);
        trainer.setSpecialization(3L);

        trainerDao.save(trainer);

        assertThat(trainerDao.findById(trainer.getUserId()))
                .isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getFirstName()).isEqualTo("Bob");
                    assertThat(found.getUsername()).isEqualTo("bob.taylor");
                });
    }

    @Test
    @DisplayName("findById should return empty when id does not exist")
    void findById_shouldReturnEmptyWhenIdDoesNotExist() {
        assertThat(trainerDao.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("findByUsername should return trainer when username exists")
    void findByUsername_shouldReturnTrainerWhenUsernameExists() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Emma");
        trainer.setLastName("Stone");
        trainer.setUsername("emma.stone");
        trainer.setPassword("pass123456");
        trainer.setActive(true);
        trainer.setSpecialization(4L);

        trainerDao.save(trainer);

        assertThat(trainerDao.findByUsername("emma.stone"))
                .isPresent()
                .hasValueSatisfying(found ->
                        assertThat(found.getLastName()).isEqualTo("Stone")
                );
    }

    @Test
    @DisplayName("findByUsername should return empty when username does not exist")
    void findByUsername_shouldReturnEmptyWhenUsernameDoesNotExist() {
        assertThat(trainerDao.findByUsername("unknown.user")).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all stored trainers")
    void findAll_shouldReturnAllStoredTrainers() {
        Trainer trainer1 = new Trainer();
        trainer1.setFirstName("Tom");
        trainer1.setLastName("Hardy");
        trainer1.setUsername("tom.hardy");
        trainer1.setPassword("pass123456");
        trainer1.setActive(true);
        trainer1.setSpecialization(1L);

        Trainer trainer2 = new Trainer();
        trainer2.setFirstName("Sara");
        trainer2.setLastName("Lee");
        trainer2.setUsername("sara.lee");
        trainer2.setPassword("pass123456");
        trainer2.setActive(true);
        trainer2.setSpecialization(2L);

        trainerDao.save(trainer1);
        trainerDao.save(trainer2);

        assertThat(trainerDao.findAll()).hasSize(2);
    }
}