package com.example.gymcrm.storage;

import com.example.gymcrm.model.Trainee;
import com.example.gymcrm.model.Trainer;
import com.example.gymcrm.model.Training;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class StorageSeedLoader {

    private static final Logger log = LoggerFactory.getLogger(StorageSeedLoader.class);
    private final ObjectMapper objectMapper;

    public StorageSeedLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void loadTrainers(String path, Map<Long, Trainer> storage){
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(path)){
            if(is == null){
                log.warn("Trainer seed file not found at classpath:{}, skipping.", path);
                return;
            }

            List<Trainer> trainers = objectMapper.readValue(is,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Trainer.class));

            trainers.forEach(t -> storage.put(t.getUserId(), t));
            log.info("Loaded {} trainers from {}", trainers.size(), path);

        }catch (Exception e){
            log.error("Failed to load trainer seed data from {}: {}", path, e.getMessage(), e);
        }
    }

    public void loadTrainees(String path, Map<Long, Trainee> storage){

        try(InputStream is = getClass().getClassLoader().getResourceAsStream(path)){

            if (is == null){
                log.warn("Trainee seed file not found at classpath:{}, skipping.", path);
                return;
            }

            List<Trainee> trainees = objectMapper.readValue(is,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Trainee.class));

            trainees.forEach(t -> storage.put(t.getUserId(), t));
            log.info("Loaded {} trainees from {}", trainees.size(), path);

        }catch (Exception e){
            log.error("Failed to load trainee seed data from {}: {}", path, e.getMessage(), e);
        }
    }

    public void loadTrainings(String path, Map<Long, Training> storage) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                log.warn("Training seed file not found at classpath:{}, skipping.", path);
                return;
            }
            List<Training> trainings = objectMapper.readValue(is,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Training.class));
            trainings.forEach(t -> storage.put(t.getId(), t));
            log.info("Loaded {} trainings from {}", trainings.size(), path);
        } catch (Exception e) {
            log.error("Failed to load training seed data from {}: {}", path, e.getMessage(), e);
        }
    }

}
