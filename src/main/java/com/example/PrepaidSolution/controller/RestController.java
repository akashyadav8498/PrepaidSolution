package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.model.User;
import com.example.PrepaidSolution.repository.UserRepository;
import com.example.PrepaidSolution.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/addUser")
public class RestController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public User createUser(@RequestBody User user) {
        user.setPassword(Utility.passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}
