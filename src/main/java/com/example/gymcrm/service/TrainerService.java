package com.example.gymcrm.service;

import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainerService {

    Trainer createProfile(String firstName, String lastName, Long specializationId);

    Trainer selectByUsername(String username, String password);

    Trainer updateProfile(String username, String password, String firstName, String lastName, Long specializationId);

    void changePassword(String username, String oldPassword, String newPassword);

    void toggleActive(String username, String password);

    List<Training> getTrainerTrainings(String username, String password, LocalDate fromDate, LocalDate toDate,
                                        String traineeName);

    void setActiveStatus(String username, String password, boolean isActive);
}
