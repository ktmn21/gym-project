package com.example.gymcrm.dto.trainer;

public class TrainerRegistrationResponse {
    private String username;
    private String password;

    public TrainerRegistrationResponse(String username, String password) {
        this.username = username; this.password = password;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}