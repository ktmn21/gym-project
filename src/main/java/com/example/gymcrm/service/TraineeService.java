package com.example.gymcrm.service;

import com.example.gymcrm.dto.trainee.TraineeRegistrationResponse;
import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface TraineeService {

    TraineeRegistrationResponse createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address);

    Trainee selectByUsername(String username);

    Trainee updateProfile(String username, String firstName, String lastName,
                           LocalDate dateOfBirth, String address);

    void changePassword(String username, String oldPassword, String newPassword);

    void toggleActive(String username);

    void deleteByUsername(String username);

    List<Training> getTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                        String trainerName, String trainingTypeName);

    List<Trainer> getTrainersNotAssigned(String username);

    Trainee updateTrainersList(String username, Set<Long> trainerIds);

    Trainee updateTrainersListByUsername(String username, Set<String> trainerUsernames);

    void setActiveStatus(String username, boolean isActive);
}
