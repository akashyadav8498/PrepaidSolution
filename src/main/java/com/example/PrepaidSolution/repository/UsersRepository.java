package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users,Long> {

    long countByRole(Users.Role role);
    Optional<Users> findByUsername(String username);

}
