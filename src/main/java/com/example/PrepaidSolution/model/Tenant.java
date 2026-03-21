package com.example.PrepaidSolution.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant")
@Getter
@Setter
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    private String phoneNumber;
    private String name;
    private String email;
    private String address;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

}
