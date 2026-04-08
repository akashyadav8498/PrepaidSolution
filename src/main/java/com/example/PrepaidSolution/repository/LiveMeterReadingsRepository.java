package com.example.PrepaidSolution.repository;

import java.util.List;

import com.example.PrepaidSolution.model.LiveMeterReadings;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveMeterReadingsRepository
        extends JpaRepository<LiveMeterReadings, Long> {

    List<LiveMeterReadings> findAllByMeterId(String meterId, Sort id);
    LiveMeterReadings findTopByMeterIdOrderByCreatedAtDesc(String meterId);
}
