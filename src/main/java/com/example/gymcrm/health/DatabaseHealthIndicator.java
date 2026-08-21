package com.example.gymcrm.health;

import com.example.gymcrm.dao.TraineeRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final TraineeRepository traineeRepository;

    public DatabaseHealthIndicator(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Override
    public Health health() {
        try {
            long count = traineeRepository.count();
            return Health.up()
                    .withDetail("traineeCount", count)
                    .withDetail("message", "Database reachable via repository")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}