package com.example.gymcrm.service.impl;

import com.example.gymcrm.dto.trainer.TrainerWorkloadRequest;
import com.example.gymcrm.service.WorkloadIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("standalone")
public class NoOpWorkloadIntegrationService implements WorkloadIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(NoOpWorkloadIntegrationService.class);

    @Override
    public void sendWorkload(TrainerWorkloadRequest request) {
        log.info("Workload queue integration is disabled (standalone profile). Skipping message for trainer={}",
                request.getUsername());
    }
}
