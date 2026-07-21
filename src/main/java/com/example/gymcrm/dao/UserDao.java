package com.example.gymcrm.dao;

import com.example.gymcrm.model.User;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public interface UserDao {
    User save(User user);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    void update(User user);
}
