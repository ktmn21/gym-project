package com.example.gymcrm.dao.implementations;

import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);
    @PersistenceContext
    private EntityManager em;

    @Override
    public Trainer save(Trainer trainer) {
        em.persist(trainer);
        log.debug("Persisted trainer id= {}", trainer.getId());
        return trainer;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        List<Trainer> trainers = em.createQuery("SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class)
                .setParameter("username", username)
                .getResultList();
        return trainers.isEmpty() ? Optional.empty() : Optional.of(trainers.get(0));
    }

    @Override
    public Trainer update(Trainer trainer) {
        Trainer t = em.merge(trainer);
        log.debug("Updated trainer id = {}", trainer.getId());
        return t;
    }

    @Override
    public List<Trainer> findAllNotAssignedToTrainee(String traineeUsername) {
        return em.createQuery(
                        "SELECT tr FROM Trainer tr WHERE tr.id NOT IN " +
                                "(SELECT t.id FROM Trainee tn JOIN tn.trainers t JOIN tn.user u WHERE u.username = :username)",
                        Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }
}
