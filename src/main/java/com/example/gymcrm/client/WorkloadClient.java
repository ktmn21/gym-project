package com.example.gymcrm.client;

import com.example.gymcrm.dto.trainer.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "workload-service")
public interface WorkloadClient {

    @PostMapping("/api/workload")
    void sendWorkload(@RequestBody TrainerWorkloadRequest request);
}
