package com.example.gymcrm.dao.implementations;

import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Training save(Training training) {
        em.persist(training);
        log.debug("Persisted training id={}", training.getId());
        return training;
    }

    @Override
    public List<Training> findTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        StringBuilder jpql = new StringBuilder(
                "SELECT tr FROM Training tr " +
                        "JOIN tr.trainee tn JOIN tn.user tnu " +
                        "JOIN tr.trainer trr JOIN trr.user tru " +
                        "JOIN tr.trainingtype tt " +
                        "WHERE tnu.username = :username");

        if (fromDate != null) jpql.append(" AND tr.trainingDate >= :fromDate");
        if (toDate != null) jpql.append(" AND tr.trainingDate <= :toDate");
        if (trainerName != null && !trainerName.isBlank())
            jpql.append(" AND (tru.firstName || ' ' || tru.lastName) LIKE :trainerName");
        if (trainingTypeName != null && !trainingTypeName.isBlank())
            jpql.append(" AND tt.trainingTypeName = :trainingTypeName");

        TypedQuery<Training> query = em.createQuery(jpql.toString(), Training.class);
        query.setParameter("username", traineeUsername);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.isBlank()) query.setParameter("trainerName", "%" + trainerName + "%");
        if (trainingTypeName != null && !trainingTypeName.isBlank()) query.setParameter("trainingTypeName", trainingTypeName);

        log.debug("Fetching trainee trainings for username={} with criteria", traineeUsername);
        return query.getResultList();
    }

    @Override
    public List<Training> findTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName) {
        StringBuilder jpql = new StringBuilder(
                "SELECT tr FROM Training tr " +
                        "JOIN tr.trainer trr JOIN trr.user tru " +
                        "JOIN tr.trainee tn JOIN tn.user tnu " +
                        "WHERE tru.username = :username");

        if (fromDate != null) jpql.append(" AND tr.trainingDate >= :fromDate");
        if (toDate != null) jpql.append(" AND tr.trainingDate <= :toDate");
        if (traineeName != null && !traineeName.isBlank())
            jpql.append(" AND (tnu.firstName || ' ' || tnu.lastName) LIKE :traineeName");

        TypedQuery<Training> query = em.createQuery(jpql.toString(), Training.class);
        query.setParameter("username", trainerUsername);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null && !traineeName.isBlank()) query.setParameter("traineeName", "%" + traineeName + "%");

        log.debug("Fetching trainer trainings for username={} with criteria", trainerUsername);
        return query.getResultList();
    }
}
