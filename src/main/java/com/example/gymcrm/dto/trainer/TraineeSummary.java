package com.example.gymcrm.dto.trainer;

import io.swagger.v3.oas.annotations.media.Schema;

public class TraineeSummary {
    private String username;
    private String firstName;
    private String lastName;

    public TraineeSummary(String username, String firstName, String lastName) {
        this.username = username; this.firstName = firstName; this.lastName = lastName;
    }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}