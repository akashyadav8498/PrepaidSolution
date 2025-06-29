package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.model.Users;
import com.example.PrepaidSolution.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/users")
public class RestController {

    @Autowired
    private UsersRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public Users createUser(@RequestBody Users user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }
}
