package com.example.PrepaidSolution.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meter_readings")
@Data
public class MeterReadings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long readingId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "meter_id", nullable = false)
//    private EnergyMeter energyMeter;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "energy_kWh", precision = 10, scale = 3)
    private BigDecimal energyKWh;

    @Column(name = "voltage", precision = 6, scale = 2)
    private BigDecimal voltage;

    @Column(name = "current", precision = 6, scale = 2)
    private BigDecimal current;

    @Column(name = "frequency", precision = 5, scale = 2)
    private BigDecimal frequency;

    @Column(name = "meter_id")
    private String meterId;
}
