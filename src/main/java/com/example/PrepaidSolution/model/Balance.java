package com.example.PrepaidSolution.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "balances")
public class Balance {

    @Id
    private Long tenantId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "tenant_id", referencedColumnName = "id")
    private Tenant tenant;

    @Column(nullable = false)
    private double currentBalance = 0.0;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Balance() {}

    public Balance(Tenant tenant) {
        this.tenant = tenant;
        this.updatedAt = LocalDateTime.now();
    }

    // ============================
    // ✅ GETTERS
    // ============================

    public Long getTenantId() {
        return tenantId;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ============================
    // ✅ SETTERS
    // ============================

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}