package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainer;
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
public class TrainerDaoImpl implements TrainerDao{

    private static final Logger log = LoggerFactory.getLogger(Trainer.class);
    private final AtomicLong idSequence = new AtomicLong();
    private Map<Long, Trainer> storage;

    @Autowired
    public void setStorage(InMemoryStorage inMemoryStorage) {
        this.storage = inMemoryStorage.getTrainerStorage();
    }

    @Override
    public Trainer save(Trainer trainer) {

        Long id = idSequence.incrementAndGet();
        trainer.setUserId(id);
        storage.put(id, trainer);
        log.debug("saved the trainer with id: {}", id);
        return trainer;

    }

    @Override
    public Trainer update(Trainer trainer) {

        storage.put(trainer.getUserId(), trainer);
        log.debug("Updated the trainer with id: {}", trainer.getUserId());

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Looking up for the trainer with id: {}", id );
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        log.debug("Looking up for the trainer with username: {}", username);
        return storage.values().stream()
                .filter(t -> t.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public List<Trainer> findAll() {
        log.debug("Fetching all Trainers");
        return new ArrayList<>(storage.values());
    }
}
