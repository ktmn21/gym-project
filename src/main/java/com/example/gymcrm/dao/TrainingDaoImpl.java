package com.example.gymcrm.dao;

import com.example.gymcrm.model.Training;
import com.example.gymcrm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TrainingDaoImpl implements TrainingDao{

    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private final AtomicLong idSequence = new AtomicLong();
    private Map<Long, Training> storage;

    @Autowired
    public void setStorage(InMemoryStorage inMemoryStorage){
        this.storage = inMemoryStorage.getTrainingStorage();
    }

    @Override
    public Training save(Training training) {

        Long id = idSequence.incrementAndGet();
        training.setId(id);
        storage.put(id, training);
        log.debug("Saved training with id: {}", id);
        return training;

    }

    @Override
    public Optional<Training> findById(Long id) {
        log.debug("Looking up for the training by id: {}", id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Training> findAll() {
        log.debug("Fetching all trainings");
        return new ArrayList<>(storage.values());
    }
}
