package com.example.gymcrm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private final Counter traineeCreatedCounter;
    private final Counter trainerCreatedCounter;
    private final Counter authFailureCounter;

    public GymMetrics(MeterRegistry registry) {
        this.traineeCreatedCounter = Counter.builder("gym_trainee_created_total")
                .description("Total trainee profiles created")
                .register(registry);
        this.trainerCreatedCounter = Counter.builder("gym_trainer_created_total")
                .description("Total trainer profiles created")
                .register(registry);
        this.authFailureCounter = Counter.builder("gym_auth_failures_total")
                .description("Total failed authentication attempts")
                .register(registry);
    }

    public void incrementTraineeCreated() { traineeCreatedCounter.increment(); }
    public void incrementTrainerCreated() { trainerCreatedCounter.increment(); }
    public void incrementAuthFailure()    { authFailureCounter.increment(); }
}