package com.example.gymcrm.service;

import jakarta.persistence.EntityManager;

public interface AuthenticationService {
    void authenticate(String username, String password);
}
