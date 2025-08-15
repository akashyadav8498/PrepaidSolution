package com.example.PrepaidSolution.components.security;

import com.example.PrepaidSolution.model.User;
import com.example.PrepaidSolution.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class CustomUserDetailsService implements UserDetailsService {
     @Autowired
     UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        User user = userRepo.findByUsernameOrEmail(input, input);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with input: " + input);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

}
