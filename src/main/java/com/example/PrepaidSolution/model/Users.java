package com.example.PrepaidSolution.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        OWNER,
        TENANT,
        ADMIN
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL)
    private Admin admin;

    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL)
    private Owner owner;

    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL)
    private Tenant tenant;

}

