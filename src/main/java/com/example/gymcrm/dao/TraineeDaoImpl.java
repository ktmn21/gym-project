package com.example.gymcrm.dao;

import com.example.gymcrm.model.Trainee;
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
public class TraineeDaoImpl implements TraineeDao {

    private final AtomicLong idSequence = new AtomicLong();
    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private Map<Long, Trainee> storage;

    @Autowired
    public void setInMemoryStorage(InMemoryStorage inMemoryStorage) {
        this.storage = inMemoryStorage.getTraineeStorage();
    }

    @Override
    public void save(Trainee trainee) {
        Long id = idSequence.incrementAndGet();
        trainee.setUserId(id);
        storage.put(id, trainee);
        log.debug("Saved trainee with id: {} and username: {}", id, trainee.getUsername());
    }

    @Override
    public void update(Trainee trainee) {
        storage.put(trainee.getUserId(), trainee);
        log.debug("Updated trainee with id: {}", trainee.getUserId());
    }

    @Override
    public void deleteById(long id) {
        Trainee target = storage.remove(id);
        if(target != null){
            log.info("Deleted trainee with id: {}", id);
        }else{
            log.warn("The trainee with id: {} not found", id);
        }
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        log.debug("looking up trainee with id: {}", id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        log.debug("looking up trainee with username: {}", username);
        return storage.values().stream()
                .filter(t -> t.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public List<Trainee> findAll() {
        log.debug("Fetching all trainees");
        return new ArrayList<>(storage.values());
    }
}