package com.example.PrepaidSolution.messaging;

import com.example.PrepaidSolution.config.RabbitMQConfig;
import com.example.PrepaidSolution.model.LiveMeterReadings;
import com.example.PrepaidSolution.repository.LiveMeterReadingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMQListener {

    final LiveMeterReadingsRepository liveMeterReadingsRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    private void receive(String message) {
        log.info("RabbitMQ: Received message -> {}", message);
        decodeAndSave(message);
    }

    private void decodeAndSave(String hexPacket) {

        hexPacket = hexPacket.substring(0,8);
        System.out.println("Meter serial id: --> " + hexPacket);

        LiveMeterReadings liveMeterReadings = new LiveMeterReadings();
        liveMeterReadings.setMeterId(String.valueOf(Long.parseLong(hexPacket, 16)));
        liveMeterReadings.setReading(hexPacket);
        liveMeterReadings.setCreatedAt(LocalDateTime.now());
        liveMeterReadingsRepository.save(liveMeterReadings);

    }
}
