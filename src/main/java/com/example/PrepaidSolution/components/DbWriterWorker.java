package com.example.PrepaidSolution.components;

import com.example.PrepaidSolution.model.LiveMeterReadings;
import com.example.PrepaidSolution.repository.LiveMeterReadingsRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DbWriterWorker {

    private final BlockingQueue<String> dbQueue;
    private final LiveMeterReadingsRepository repo;

    @PostConstruct
    public void start() {
        Executors.newFixedThreadPool(2).submit(() -> {
            while (true) {
                try {
                    String hexPacket = dbQueue.take();
                    save(hexPacket);
                } catch (Exception e) {
                    log.error("❌ DB Worker error", e);
                }
            }
        });
    }

    @Transactional
    private void save(String message) {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String meterId = jsonObject
                .getAsJsonObject("live_data")
                .get("sn")
                .getAsString();

        LiveMeterReadings entity = new LiveMeterReadings();
        entity.setMeterId(meterId);
        entity.setReading(message);
        entity.setCreatedAt(LocalDateTime.now());

        repo.save(entity);
    }
}

