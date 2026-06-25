package com.example.gymcrm.service;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsernamePasswordGenerator unit tests")
class UsernamePasswordGeneratorTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TraineeDao traineeDao;

    private UsernamePasswordGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new UsernamePasswordGenerator();
        generator.setTrainerDao(trainerDao);
        generator.setTraineeDao(traineeDao);
    }

    @Test
    @DisplayName("generateUserName: returns base when no collision")
    void generateUserName_noCollision() {
        String firstName = "John";
        String lastName = "Smith";
        String base = "John.Smith";

        // No trainer or trainee with this username
        when(traineeDao.findByUsername(base)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(base)).thenReturn(Optional.empty());

        String result = generator.generateUserName(firstName, lastName);

        assertEquals(base, result);
        verify(traineeDao).findByUsername(base);
        verify(trainerDao).findByUsername(base);
    }
    @Test
    @DisplayName("generateUserName: adds .1 when base username is taken")
    void generateUserName_singleCollisionAddsDotOne() {
        String firstName = "John";
        String lastName = "Smith";
        String base = "John.Smith";
        String candidate1 = base + ".1";

        when(traineeDao.findByUsername(base)).thenReturn(Optional.of(new Trainee()));

        // .1 is free -> both DAOs are checked
        when(traineeDao.findByUsername(candidate1)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(candidate1)).thenReturn(Optional.empty());

        String result = generator.generateUserName(firstName, lastName);

        assertEquals("John.Smith.1", result);

        verify(traineeDao).findByUsername(base);
        verify(trainerDao, never()).findByUsername(base);

        verify(traineeDao).findByUsername(candidate1);
        verify(trainerDao).findByUsername(candidate1);
    }

    @Test
    @DisplayName("generateUserName: skips taken .1 and .2, returns .3")
    void generateUserName_multipleCollisionsSkipsToThree() {
        String firstName = "John";
        String lastName = "Smith";
        String base = "John.Smith";

        String candidate1 = base + ".1";
        String candidate2 = base + ".2";
        String candidate3 = base + ".3";

        when(traineeDao.findByUsername(base)).thenReturn(Optional.of(new Trainee()));

        // .1 free in trainee, but taken in trainer -> both checked
        when(traineeDao.findByUsername(candidate1)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(candidate1)).thenReturn(Optional.of(new Trainer()));

        // .2 taken by trainee -> short-circuit, trainerDao not called for .2
        when(traineeDao.findByUsername(candidate2)).thenReturn(Optional.of(new Trainee()));

        // .3 free -> both checked
        when(traineeDao.findByUsername(candidate3)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(candidate3)).thenReturn(Optional.empty());

        String result = generator.generateUserName(firstName, lastName);

        assertEquals("John.Smith.3", result);

        verify(traineeDao).findByUsername(base);
        verify(trainerDao, never()).findByUsername(base);

        verify(traineeDao).findByUsername(candidate1);
        verify(trainerDao).findByUsername(candidate1);

        verify(traineeDao).findByUsername(candidate2);
        verify(trainerDao, never()).findByUsername(candidate2);

        verify(traineeDao).findByUsername(candidate3);
        verify(trainerDao).findByUsername(candidate3);
    }

    @RepeatedTest(5)
    @DisplayName("generatePassword: returns 10-character alphanumeric string")
    void generatePassword_returns10CharAlphaNumeric() {
        String password = generator.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());

        String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (char c : password.toCharArray()) {
            assertTrue(allowed.indexOf(c) >= 0, "Password contains invalid character: " + c);
        }
    }
}