package com.example.gymcrm.service.impl;

import com.example.gymcrm.dto.trainer.TrainerWorkloadRequest;
import com.example.gymcrm.service.WorkloadMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkloadMessageProducerImpl implements WorkloadMessageProducer {

    private final JmsTemplate jmsTemplate;

    @Override
    public void sendWorkLoad(TrainerWorkloadRequest request) {
        jmsTemplate.convertAndSend("workload.queue", request);
    }
}

