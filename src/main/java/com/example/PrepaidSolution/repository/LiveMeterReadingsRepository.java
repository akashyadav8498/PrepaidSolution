package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.LiveMeterReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveMeterReadingsRepository extends JpaRepository<LiveMeterReadings, Long> {
}
