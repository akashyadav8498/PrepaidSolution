package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.PG;
import com.example.PrepaidSolution.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PGRepository extends JpaRepository<PG, Long> {
    List<PG> findAllByOwner_Id(Long ownerId);
}
