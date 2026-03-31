package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.PG;
import com.example.PrepaidSolution.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PGRepository extends JpaRepository<PG, Long> {
    List<PG> findAllByOwner_Id(Long ownerId);

    @Query("SELECT COUNT(p) FROM PG p WHERE p.owner.id = :ownerId")
    int countPgsByOwnerId(Long ownerId);

    List<PG> findByOwnerId(Long ownerId);
}
