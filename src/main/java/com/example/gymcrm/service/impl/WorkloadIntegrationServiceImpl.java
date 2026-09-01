package com.example.gymcrm.service.impl;


import com.example.gymcrm.client.WorkloadClient;
import com.example.gymcrm.dto.trainer.TrainerWorkloadRequest;
import com.example.gymcrm.service.WorkloadIntegrationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkloadIntegrationServiceImpl implements WorkloadIntegrationService {

    private final WorkloadClient workloadClient;

    @Override
    @CircuitBreaker(name = "workloadService", fallbackMethod = "fallback")
    public void sendWorkload(TrainerWorkloadRequest request) {
        workloadClient.sendWorkload(request);
    }
    private void fallback(TrainerWorkloadRequest request, Throwable t) {
        System.err.println("Workload service unavailable: " + t.getMessage());
    }
}