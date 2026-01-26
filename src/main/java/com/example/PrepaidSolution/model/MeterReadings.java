package com.example.PrepaidSolution.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meter_readings")
@Getter
@Setter
public class MeterReadings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long readingId;

    @Column(name = "meter_id", length = 255)
    private String meterId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "energy_k_w_h", precision = 10, scale = 3)
    private BigDecimal energyKWh;

    @Column(name = "voltage", precision = 6, scale = 2)
    private BigDecimal voltage;

    @Column(name = "current", precision = 6, scale = 2)
    private BigDecimal current;

    @Column(name = "frequency", precision = 5, scale = 2)
    private BigDecimal frequency;

}