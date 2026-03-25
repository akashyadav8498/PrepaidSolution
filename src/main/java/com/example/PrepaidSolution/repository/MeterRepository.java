package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterRepository extends JpaRepository<Meter, Long> {
}