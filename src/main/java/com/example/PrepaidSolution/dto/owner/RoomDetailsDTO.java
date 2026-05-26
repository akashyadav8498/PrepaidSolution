package com.example.PrepaidSolution.dto.owner;

public class RoomDetailsDTO {

    private String roomNumber;
    private Long tenantId;
    private String tenantName;
    private double balance;
    private String meterId;
    private boolean relayStatus;
    private boolean connectionStatus;
    private Double eb;
    private Double dg;


    public RoomDetailsDTO(String roomNumber,Long tenantId, String tenantName, double balance, String meterId, boolean relayStatus,boolean connectionStatus, double eb, double dg) {
        this.roomNumber = roomNumber;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.balance = balance;
        this.meterId = meterId;
        this.relayStatus = relayStatus;
        this.connectionStatus = connectionStatus;
        this.eb = eb;
        this.dg = dg;
    }

    // ✅ GETTERS ONLY

    public String getRoomNumber() {
        return roomNumber;
    }

    public Long getTenantId(){
        return tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public double getBalance() {
        return balance;
    }

    public String getMeterId() {
        return meterId;
    }

    public boolean isRelayStatus() {  // ✅ boolean getter convention
        return relayStatus;
    }

    public boolean isConnectionStatus() {
        return connectionStatus;
    }

    public Double getEb() {
        return eb;
    }

    public Double getDg() {
        return dg;
    }

    public void setEb(Double eb) {
        this.eb = eb;
    }

    public void setDg(Double dg) {
        this.dg = dg;
    }
}