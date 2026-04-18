package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.dto.owner.RoomDetailsDTO;
import com.example.PrepaidSolution.model.Meter;
import com.example.PrepaidSolution.model.PG;
import com.example.PrepaidSolution.model.Room;
import com.example.PrepaidSolution.model.Tenant;
import com.example.PrepaidSolution.service.OwnerService;

import jakarta.servlet.http.HttpSession;
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


    // ✅ Get room details by PG ID
    @GetMapping("/pg/{pgId}/rooms")
    public List<RoomDetailsDTO> getRoomDetailsByPg(@PathVariable Long pgId) {
        return ownerService.getRoomDetailsByPg(pgId);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getOwnerStats(HttpSession session) {
        String email = (String) session.getAttribute("email");
        System.out.println("Email: ->" + email);
        return ResponseEntity.ok(ownerService.getOwnerStats(email));
    }

    @GetMapping("/{ownerId}/pgs")
    public ResponseEntity<List<PG>> getAllPgs(@PathVariable Long ownerId) {
                return ResponseEntity.ok(ownerService.getAllPgs(ownerId));
    }
}