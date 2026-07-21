package com.example.gymcrm.dao.implementations;

import com.example.gymcrm.dao.TrainingTypeDao;
import com.example.gymcrm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<TrainingType> findAll() {
        return em.createQuery("FROM TrainingType", TrainingType.class).getResultList();
    }

    @Override
    public Optional<TrainingType> findByName(String name) {
        List<TrainingType> results = em.createQuery(
                        "FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", name)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

}
