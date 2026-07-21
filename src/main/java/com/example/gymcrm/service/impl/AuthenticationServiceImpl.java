package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.UserDao;
import com.example.gymcrm.exceptions.AuthenticationException;
import com.example.gymcrm.model.User;
import com.example.gymcrm.service.AuthenticationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @PersistenceContext
    private EntityManager em;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final UserDao userDao;

    public AuthenticationServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void authenticate(String username, String password) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: no user with username={}", username);
                    return new AuthenticationException("Invalid username or password");
                });

        if (!user.getPassword().equals(password)) {
            log.warn("Authentication failed: password mismatch for username={}", username);
            throw new AuthenticationException("Invalid username or password");
        }
        log.debug("Authentication succeeded for username={}", username);
    }
}
