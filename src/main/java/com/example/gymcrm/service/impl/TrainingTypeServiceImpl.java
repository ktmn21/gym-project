package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TrainingTypeRepository;
import com.example.gymcrm.model.TrainingType;
import com.example.gymcrm.service.TrainingTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeServiceImpl(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> getAll() {
        return trainingTypeRepository.findAll();
    }
}