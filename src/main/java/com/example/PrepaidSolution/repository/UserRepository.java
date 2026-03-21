package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    List<User> findAllByRoleEquals(User.Role role);
    User findByUsername(String username);

    long countByRole(User.Role role);
}
