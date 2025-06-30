package com.example.PrepaidSolution.components.security;

import com.example.PrepaidSolution.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        String hql = "FROM User u WHERE u.username = :input OR u.email = :input";
        List<User> result = entityManager.createQuery(hql, User.class)
                .setParameter("input", input)
                .getResultList();

        if (result.isEmpty()) {
            throw new UsernameNotFoundException("User not found with input: " + input);
        }

        User user = result.get(0); // My-entity
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPasswordHash(), authorities);
    }
}
