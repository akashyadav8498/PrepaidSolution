package com.example.PrepaidSolution.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "pg")
@Getter
@Setter
public class PG {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @OneToMany(mappedBy = "pg", cascade = CascadeType.ALL)
    private List<Room> rooms;


}

