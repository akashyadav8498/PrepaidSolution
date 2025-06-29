package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.MeterReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeterReadingsRepo extends JpaRepository<MeterReadings, Long> {
}
