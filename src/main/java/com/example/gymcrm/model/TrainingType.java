package com.example.gymcrm.model;

import jakarta.persistence.*;

/**
 * Constant catalog table. Rows are seeded once via docker-entrypoint-initdb.d
 * SQL script and must never be inserted/updated/deleted from the application.
 */
@Entity
@Table(name = "training_type")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;

    public TrainingType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrainingTypeName() { return trainingTypeName; }
    public void setTrainingTypeName(String trainingTypeName) { this.trainingTypeName = trainingTypeName; }
}
