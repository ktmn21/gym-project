package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    @Query("select t from Trainee t" +
            " left join fetch t.trainers tr" +
            " left join fetch tr.user" +
            " left join fetch tr.specialization" +
            " where t.user.username = :username")
    Optional<Trainee> findByUserName(@Param("username") String username);
}