package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.room.pg.owner.id = :ownerId")
    int countTenantsByOwnerId(Long ownerId);
}
