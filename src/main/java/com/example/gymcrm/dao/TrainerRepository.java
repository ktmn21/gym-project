package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("select distinct t from Trainer t" +
            " left join fetch t.user" +
            " left join fetch t.specialization" +
            " left join fetch t.trainees tr" +
            " left join fetch tr.user" +
            " where t.user.username = :username")
    Optional<Trainer> findByUsername(@Param("username") String username);

    @Query("SELECT tr FROM Trainer tr WHERE tr.id NOT IN " +
            "(SELECT t.id FROM Trainee tn JOIN tn.trainers t JOIN tn.user u WHERE u.username = :username)")
    List<Trainer> findAllNotAssignedToTrainee(@Param("username") String username);

}