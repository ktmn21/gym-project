package com.example.gymcrm.service;

public interface AuthenticationService {
    void authenticate(String username, String password);
    void changePassword(String username, String oldPassword, String newPassword);
}
