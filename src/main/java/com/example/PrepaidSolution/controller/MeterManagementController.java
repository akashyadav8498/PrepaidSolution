package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.service.MeterManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/meter/")
public class MeterManagementController {

    @Autowired
    private MeterManagementService meterManagementService;

    @GetMapping("/get_live_readings")
    public ResponseEntity<?> getLiveReadings() {
        return meterManagementService.getLiveReadings();
    }

    @PostMapping("/add_owner")
    public ResponseEntity<?> addOwner(@RequestBody Map<String,String> requestMap) {
        return meterManagementService.addOwner(requestMap);
    }

    @PostMapping("/add_pg")
    public ResponseEntity<?> addPG(@RequestBody Map<String,String> requestMap) {
        return meterManagementService.addPG(requestMap);
    }

    @PostMapping("/add_room")
    public ResponseEntity<?> addRoom(@RequestBody Map<String,String> requestMap) {
        return meterManagementService.addRoom(requestMap);
    }

    @GetMapping("/get_rooms")
    public ResponseEntity<?> getRooms(@RequestParam("pg_id") String pgId) {
        return meterManagementService.getRooms(pgId);
    }
}
