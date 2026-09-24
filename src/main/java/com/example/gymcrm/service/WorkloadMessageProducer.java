package com.example.gymcrm.service;

import com.example.gymcrm.dto.trainer.TrainerWorkloadRequest;

public interface WorkloadMessageProducer {
    void sendWorkLoad(TrainerWorkloadRequest request);
}
