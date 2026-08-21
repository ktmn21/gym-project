package com.example.gymcrm.service;

import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainerService {

    Trainer createProfile(String firstName, String lastName, Long specializationId);

    Trainer selectByUsername(String username);

    Trainer updateProfile(String username, String firstName, String lastName, Long specializationId);

    void changePassword(String username, String oldPassword, String newPassword);

    void toggleActive(String username);

    List<Training> getTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                        String traineeName);

    void setActiveStatus(String username, boolean isActive);
}
