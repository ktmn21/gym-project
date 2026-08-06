package com.example.gymcrm.health;

import com.example.gymcrm.dao.TrainingTypeRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        long types = trainingTypeRepository.findAll().size();
        return Health.up()
                .withDetail("trainingTypesAvailable", types)
                .build();
    }
}