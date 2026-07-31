package com.example.gymcrm.service;

import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface TraineeService {

    Trainee createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address);

    Trainee selectByUsername(String username, String password);

    Trainee updateProfile(String username, String password, String firstName, String lastName,
                           LocalDate dateOfBirth, String address);

    void changePassword(String username, String oldPassword, String newPassword);

    void toggleActive(String username, String password);

    void deleteByUsername(String username, String password);

    List<Training> getTraineeTrainings(String username, String password, LocalDate fromDate, LocalDate toDate,
                                        String trainerName, String trainingTypeName);

    List<Trainer> getTrainersNotAssigned(String username, String password);

    Trainee updateTrainersList(String username, String password, Set<Long> trainerIds);

    Trainee updateTrainersListByUsername(String username, String password, Set<String> trainerUsernames);

    void setActiveStatus(String username, String password, boolean isActive);
}
