package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeterTypeRepository extends JpaRepository<MeterType, Long> {
}
