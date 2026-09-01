package com.example.gymcrm.service;

import com.example.gymcrm.dto.trainer.TrainerWorkloadRequest;

public interface WorkloadIntegrationService {

    void sendWorkload(TrainerWorkloadRequest request);
}
