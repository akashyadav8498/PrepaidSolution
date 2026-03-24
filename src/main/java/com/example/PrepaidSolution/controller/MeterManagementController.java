package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.dto.pg.PGDropdownDTO;
import com.example.PrepaidSolution.model.Meter;
import com.example.PrepaidSolution.model.PG;
import com.example.PrepaidSolution.model.Room;
import com.example.PrepaidSolution.model.User;
import com.example.PrepaidSolution.repository.PGRepository;
import com.example.PrepaidSolution.service.MeterManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/meter/")
public class MeterManagementController {

    @Autowired
    private MeterManagementService meterManagementService;
    private PGRepository pgRepository;

//    @PostMapping("/add_meter")
//    public Meter createMeter(@RequestBody Meter meter,
//                             @RequestParam Long roomId) {
//
//        return meterManagementService.saveMeter(meter, roomId);
//    }

    @PostMapping("/add_meter/{roomId}")
    public Meter createMeter(@RequestBody Meter meter,
                             @PathVariable Long roomId) {

        return meterManagementService.saveMeter(meter, roomId);
    }

    @GetMapping("/rooms/by-pg/{pgId}")
    public ResponseEntity<List<Room>> getRoomsByPg(@PathVariable Long pgId){
        return ResponseEntity.ok(
                meterManagementService.getRoomsByPg(pgId)
        );
    }

    @GetMapping("/pg/by-owner/{ownerId}")
    public ResponseEntity<List<PGDropdownDTO>> getPgsByOwner(
            @PathVariable Long ownerId) {

        return ResponseEntity.ok(
                meterManagementService.getPgsByOwner(ownerId)
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Long>> getDashboardCounts() {

        Map<String, Long> counts = meterManagementService.getDashboardCounts();

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(meterManagementService.getAllUsers
                ());
    }

    @GetMapping("/{meterId}")
    public ResponseEntity<?> getMeterReadings(@PathVariable String meterId) {
        return meterManagementService.getMeterReadings(meterId);
    }

    @GetMapping("/get_onload_data")
    public ResponseEntity<?> getOnloadData(HttpServletRequest httpServletRequest) {
        return meterManagementService.getOnloadData(httpServletRequest);
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

    @PostMapping("/add_tenant")
    public ResponseEntity<?> addTenant(@RequestBody Map<String,String> requestMap) {
        return meterManagementService.addTenant(requestMap);
    }

    @GetMapping("/get_rooms")
    public ResponseEntity<?> getRooms(@RequestParam("pg_id") String pgId) {
        return meterManagementService.getRooms(pgId);
    }
}
