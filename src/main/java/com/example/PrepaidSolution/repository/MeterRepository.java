package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    @Query("SELECT COUNT(m) FROM Meter m WHERE m.room.pg.owner.id = :ownerId")
    int countMetersByOwnerId(Long ownerId);

    List<Meter> findByRoomId(Long id);
}