package com.example.gymcrm.dao.implementations;

import com.example.gymcrm.dao.UserDao;
import com.example.gymcrm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    private EntityManager em;

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        em.persist(user);
        log.debug("Persisted user with username={}", user.getUsername());
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> results = em.createQuery("FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public void update(User user) {
        em.merge(user);
        log.debug("Updated user id={}", user.getId());
    }
}
