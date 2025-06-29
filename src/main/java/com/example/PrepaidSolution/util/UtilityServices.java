package com.example.PrepaidSolution.util;

import com.example.PrepaidSolution.model.MeterReadings;
import com.example.PrepaidSolution.repository.MeterReadingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

@Service
public class UtilityServices {

    @Autowired
    private MeterReadingsRepo repository;

    public void decodeAndSave(String hexPacket) {
        byte[] packet = hexStringToByteArray(hexPacket);
        ByteBuffer buffer = ByteBuffer.wrap(packet);

        try {
            buffer.get(); // Start Byte
            buffer.get(); // Start Byte 2
            buffer.get(); // Device Type

            // Device ID (4 bytes)
            byte[] idBytes = new byte[4];
            buffer.get(idBytes);
            String meterId = bytesToHex(idBytes);

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

            repository.save(reading);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode hex packet: " + e.getMessage(), e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    private byte[] hexStringToByteArray(String s) {
        s = s.replaceAll("\\s+", ""); // Remove any spaces
        int len = s.length();
        if (len % 2 != 0) throw new IllegalArgumentException("Invalid hex string");
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
