package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.UserRepository;
import com.example.gymcrm.exceptions.AuthenticationException;
import com.example.gymcrm.exceptions.EntityNotFoundException;
import com.example.gymcrm.model.User;
import com.example.gymcrm.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final UserRepository userRepository;

    public AuthenticationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
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

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticate(username, oldPassword);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found username=" + username));
        user.setPassword(newPassword);
        userRepository.save(user);
        log.info("Password changed for username={}", username);
    }
}
