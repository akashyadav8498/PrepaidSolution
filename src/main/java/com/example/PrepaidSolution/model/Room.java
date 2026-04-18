package com.example.PrepaidSolution.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "pg_id", nullable = false)
    private PG pg;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnore
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

    @OneToOne(mappedBy = "room")
    @JsonIgnore
    private Meter meter;

    // ============================
    // ✅ GETTERS
    // ============================

    public Long getId() {
        return id;
    }

    public PG getPg() {
        return pg;
    }

    public List<Tenant> getTenants() {
        return tenants;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Meter getMeter(){
        return meter;
    }

    // ============================
    // ✅ SETTERS
    // ============================

    public void setId(Long id) {
        this.id = id;
    }

    public void setPg(PG pg) {
        this.pg = pg;
    }

    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}