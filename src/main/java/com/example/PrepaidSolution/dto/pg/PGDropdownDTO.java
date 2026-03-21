package com.example.PrepaidSolution.dto.pg;

public class PGDropdownDTO {

    private Long id;
    private String name;

    // Constructor
    public PGDropdownDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter for id
    public Long getId() {
        return id;
    }

    // Getter for name
    public String getName() {
        return name;
    }
}
