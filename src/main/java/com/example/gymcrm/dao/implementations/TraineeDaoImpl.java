package com.example.gymcrm.dao.implementations;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.model.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    @PersistenceContext
    private EntityManager em;


    @Override
    public Trainee save(Trainee trainee) {
        em.persist(trainee);
        log.debug("Persisted trainee id={}", trainee.getId());
        return trainee;
    }

    @Override
    public Optional<Trainee> findByUserName(String username) {
        return em.createQuery("SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                .setParameter("username", username)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Trainee update(Trainee trainee) {
        Trainee merged = em.merge(trainee);
        log.debug("Updated trainee id={}", trainee.getId());
        return merged;
    }

    @Override
    public void delete(Trainee trainee) {
        Trainee managed = em.contains(trainee) ? trainee : em.merge(trainee);
        em.remove(managed);
        log.debug("Hard-deleted trainee id={} (cascades to trainings)", trainee.getId());
    }

    @Override
    public List<Trainee> findAll() {
        return em.createQuery("SELECT t FROM Trainee t", Trainee.class)
                .getResultList();
    }
}
