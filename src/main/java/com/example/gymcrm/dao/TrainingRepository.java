package com.example.gymcrm.dao;

import com.example.gymcrm.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("SELECT tr FROM Training tr " +
            "JOIN tr.trainee tn JOIN tn.user tnu " +
            "JOIN tr.trainer trr JOIN trr.user tru " +
            "JOIN tr.trainingType tt " +
            "WHERE tnu.username = :username " +
            "AND (:fromDate IS NULL OR tr.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR tr.trainingDate <= :toDate) " +
            "AND (:trainerName IS NULL OR " +
            "     (tru.firstName || ' ' || tru.lastName) LIKE CONCAT('%', :trainerName, '%')) " +
            "AND (:trainingTypeName IS NULL OR tt.trainingTypeName = :trainingTypeName)")
    List<Training> findTraineeTrainings(@Param("username") String username,
                                        @Param("fromDate") LocalDate fromDate,
                                        @Param("toDate") LocalDate toDate,
                                        @Param("trainerName") String trainerName,
                                        @Param("trainingTypeName") String trainingTypeName);

    @Query("SELECT tr FROM Training tr " +
            "JOIN tr.trainer trr JOIN trr.user tru " +
            "JOIN tr.trainee tn JOIN tn.user tnu " +
            "WHERE tru.username = :username " +
            "AND (:fromDate IS NULL OR tr.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR tr.trainingDate <= :toDate) " +
            "AND (:traineeName IS NULL OR " +
            "     (tnu.firstName || ' ' || tnu.lastName) LIKE CONCAT('%', :traineeName, '%'))")
    List<Training> findTrainerTrainings(@Param("username") String username,
                                        @Param("fromDate") LocalDate fromDate,
                                        @Param("toDate") LocalDate toDate,
                                        @Param("traineeName") String traineeName);

}