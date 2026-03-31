package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.model.Meter;
import com.example.PrepaidSolution.model.PG;
import com.example.PrepaidSolution.model.Room;
import com.example.PrepaidSolution.model.Tenant;
import com.example.PrepaidSolution.service.OwnerService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    // ================== DASHBOARD STATS ==================
    @GetMapping("/{ownerId}/stats")
    public ResponseEntity<?> getOwnerStats(@PathVariable Long ownerId) {
        return ResponseEntity.ok(ownerService.getOwnerStats(ownerId));
    }

    @GetMapping("/{ownerId}/pgs")
    public ResponseEntity<List<PG>> getAllPgs(@PathVariable Long ownerId) {
//        return ResponseEntity.ok(ownerService.getAllPgs(ownerId));
                return ResponseEntity.ok(ownerService.getAllPgs(ownerId));
    }
//
//    // ================== ROOMS ==================
//    @GetMapping("/{ownerId}/rooms")
//    public ResponseEntity<List<Room>> getAllRooms(@PathVariable Long ownerId) {
//        return ResponseEntity.ok(ownerService.getAllRooms(ownerId));
//    }
//
//    // ================== TENANTS ==================
//    @GetMapping("/{ownerId}/tenants")
//    public ResponseEntity<List<Tenant>> getAllTenants(@PathVariable Long ownerId) {
//        return ResponseEntity.ok(ownerService.getAllTenants(ownerId));
//    }
//
//    // ================== METERS ==================
//    @GetMapping("/{ownerId}/meters")
//    public ResponseEntity<List<Meter>> getAllMeters(@PathVariable Long ownerId) {
//        return ResponseEntity.ok(ownerService.getAllMeters(ownerId));
//    }
}