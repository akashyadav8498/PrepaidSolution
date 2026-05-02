package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.Owner;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    @Query("SELECT o.name FROM Owner o WHERE o.id = :ownerId")
    String findNameById(@Param("ownerId") Long ownerId);
}
