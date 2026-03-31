package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.model.Owner;
import com.example.PrepaidSolution.model.PG;
import com.example.PrepaidSolution.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OwnerService {

    @Autowired
    private LiveMeterReadingsRepository liveMeterReadingsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PGRepository pgRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    private EmailService emailService;

    public Map<String, Object> getOwnerStats(Long ownerId){
        int totalPgs = pgRepository.countPgsByOwnerId(ownerId);
        int totalRooms = roomRepository.countRoomsByOwnerId(ownerId);
        int totalTenants = tenantRepository.countTenantsByOwnerId(ownerId);
        int totalMeters = meterRepository.countMetersByOwnerId(ownerId);
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        String ownerName = owner.getName();
        String ownerEmail = owner.getEmail();
        String ownerMobile = owner.getMobile();

        Map<String, Object> response = new HashMap<>();
        response.put("totalPgs", totalPgs);
        response.put("totalRooms", totalRooms);
        response.put("totalTenants", totalTenants);
        response.put("totalMeters", totalMeters);
        response.put("ownerName", ownerName);
        response.put("ownerEmail", ownerEmail);
        response.put("ownerMobile", ownerMobile);

        return response;
    }


    public List<PG> getAllPgs(Long ownerId) {
        return pgRepository.findByOwnerId(ownerId);
    }
}
