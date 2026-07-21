package com.example.gymcrm.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainer")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

    public Trainer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TrainingType getSpecialization() { return specialization; }
    public void setSpecialization(TrainingType specialization) { this.specialization = specialization; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Set<Trainee> getTrainees() { return trainees; }
    public void setTrainees(Set<Trainee> trainees) { this.trainees = trainees; }
}
