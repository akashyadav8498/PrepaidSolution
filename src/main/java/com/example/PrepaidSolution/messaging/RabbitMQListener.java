package com.example.PrepaidSolution.messaging;

import com.example.PrepaidSolution.model.MeterReadings;
import com.example.PrepaidSolution.repository.MeterReadingsRepository;
import com.example.PrepaidSolution.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

@Component
@Slf4j
public class RabbitMQListener {

    @Autowired
    MeterReadingsRepository meterReadingsRepository;

//    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    private void receive(String message) {
        log.info("RabbitMQ: Received message -> {}", message);
        decodeAndSave(message);
    }

    private void decodeAndSave(String hexPacket) {
        byte[] packet = Utility.hexStringToByteArray(hexPacket);
        ByteBuffer buffer = ByteBuffer.wrap(packet);

        try {
            buffer.get(); // Start Byte
            buffer.get(); // Start Byte 2
            buffer.get(); // Device Type

            // Device ID (4 bytes)
            byte[] idBytes = new byte[4];
            buffer.get(idBytes);
            String meterId = Utility.bytesToHex(idBytes);

            // Timestamp - Assume values are NOT in BCD
            int year = Byte.toUnsignedInt(buffer.get()) + 2000;
            int month = Byte.toUnsignedInt(buffer.get());
            int day = Byte.toUnsignedInt(buffer.get());
            int hour = Byte.toUnsignedInt(buffer.get());
            int minute = Byte.toUnsignedInt(buffer.get());
            int second = Byte.toUnsignedInt(buffer.get());

            LocalDateTime timestamp = LocalDateTime.of(year, month, day, hour, minute, second);

            // kWh (4 bytes as int, divide by 100)
            int kWhRaw = buffer.getInt();
            BigDecimal energy = BigDecimal.valueOf(kWhRaw).movePointLeft(2);

            // Voltage (2 bytes as unsigned short, divide by 10)
            int voltageRaw = Short.toUnsignedInt(buffer.getShort());
            BigDecimal voltage = BigDecimal.valueOf(voltageRaw).movePointLeft(1);

            // Current (2 bytes, divide by 1000)
            int currentRaw = Short.toUnsignedInt(buffer.getShort());
            BigDecimal current = BigDecimal.valueOf(currentRaw).movePointLeft(3);

            // Skip: Active Power (2 bytes)
            buffer.getShort();

            // Skip: Power Factor (1 byte)
            buffer.get();

            // Frequency (2 bytes, divide by 100)
            int freqRaw = Short.toUnsignedInt(buffer.getShort());
            BigDecimal frequency = BigDecimal.valueOf(freqRaw).movePointLeft(2);

            // Skip: Relay Status (1 byte), Event Status (1 byte), End Byte (1 byte), CRC (2 bytes)
            buffer.get(); // Relay status
            buffer.get(); // Event status
            buffer.get(); // End byte
            buffer.getShort(); // CRC

            // Save to DB
            MeterReadings reading = new MeterReadings();
            reading.setMeterId(meterId);
            reading.setTimestamp(timestamp);
            reading.setEnergyKWh(energy);
            reading.setVoltage(voltage);
            reading.setCurrent(current);
            reading.setFrequency(frequency);

            meterReadingsRepository.save(reading);
        } catch (Exception e) {
            log.error("Failed to decode hex packet: {}", e.getMessage());
        }
    }
}
