package com.example.PrepaidSolution.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "live_meter_readings")
@Getter
@Setter
public class LiveMeterReadings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String meterId;

    private String reading;

    @Column(updatable = false)
    private LocalDateTime createdAt;
}