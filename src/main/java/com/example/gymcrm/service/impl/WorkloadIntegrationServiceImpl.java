package com.example.gymcrm.service.impl;

import com.example.gymcrm.dto.trainer.TrainerWorkloadRequest;
import com.example.gymcrm.service.WorkloadIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class WorkloadIntegrationServiceImpl implements WorkloadIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadIntegrationServiceImpl.class);

    private final JmsTemplate jmsTemplate;

    @Value("${workload.queue.name}")
    private String queueName;

    public WorkloadIntegrationServiceImpl(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void sendWorkload(TrainerWorkloadRequest request) {
        try {
            jmsTemplate.convertAndSend(queueName, request);
            log.info("Published workload message: trainer={}, action={}",
                    request.getUsername(), request.getActionType());
        } catch (Exception e) {
            log.error("Failed to publish workload message for trainer={}: {}",
                    request.getUsername(), e.getMessage(), e);
        }
    }
}