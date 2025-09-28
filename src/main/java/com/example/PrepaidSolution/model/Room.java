package com.example.PrepaidSolution.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pg_id", nullable = false)
    private PG pg;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Tenant> tenants;

    @Column(nullable = false)
    private String roomNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        OCCUPIED,
        VACANT
    }
}

