package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepo extends JpaRepository<User,Long> {
}
