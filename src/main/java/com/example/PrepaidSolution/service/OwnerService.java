package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.dto.owner.RoomDetailsDTO;
import com.example.PrepaidSolution.dto.owner.RoomDetailsDTO;
import com.example.PrepaidSolution.model.*;
import com.example.PrepaidSolution.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private BalanceRepository balanceRepository;

    public List<RoomDetailsDTO> getRoomDetailsByPg(Long pgId) {

        PG pg = pgRepository.findById(pgId).orElse(null);

        if (pg == null) {
            return new ArrayList<>();
        }

        List<RoomDetailsDTO> result = new ArrayList<>();

        for (Room room : pg.getRooms()) {

            // Room Number
            String roomNumber = room.getRoomNo();

            // Tenant
            Tenant tenant = null;
            if (room.getTenants() != null && !room.getTenants().isEmpty()) {
                tenant = room.getTenants().get(0);
            }
            String tenantName = (tenant != null) ? tenant.getName() : "Unknown";

            // Balance
            double balance = 0.0;

            if (tenant != null) {
                balance = balanceRepository.findById(tenant.getId())
                        .map(Balance::getCurrentBalance)
                        .orElse(0.0);
            }

            // Meter + Relay Status + Connection Status
            String meterId = "N/A";
            boolean relayStatus = false;
            boolean connectionStatus = false;
            double eb = 0;
            double dg = 0;
            List<Meter> meters = meterRepository.findByRoomId(room.getId());

            if (meters != null && !meters.isEmpty()) {

                Meter meter = meters.get(0);
                meterId = meter.getSerialNo();

                //  Get latest reading
                LiveMeterReadings reading = liveMeterReadingsRepository
                        .findTopByMeterIdOrderByCreatedAtDesc(meter.getSerialNo());

                if (reading != null && reading.getReading() != null) {

                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(reading.getReading());

                        JsonNode liveData = root.get("live_data");

                        if (liveData != null) {

                            if (liveData.has("rls")) {
                                int rls = liveData.get("rls").asInt();
                                relayStatus = ((rls & 3) != 0); // (bitwise)
                            }

                            if (liveData.has("kwh1")) {
                                eb = liveData.get("kwh1").asDouble();
                            }

                            if (liveData.has("kwh2")) {
                                dg = liveData.get("kwh2").asDouble();
                            }


                        }



                        if (reading.getCreatedAt() != null) {

                            LocalDateTime now = LocalDateTime.now();
                            LocalDateTime lastReadingTime = reading.getCreatedAt();

                            long minutes = Duration.between(lastReadingTime, now).toMinutes();

                            if (minutes <= 30) {
                                connectionStatus = true;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // DTO
            RoomDetailsDTO dto = new RoomDetailsDTO(
                    roomNumber,
                    tenantName,
                    balance,
                    meterId,
                    relayStatus,
                    connectionStatus,
                    eb,
                    dg
            );
            result.add(dto);
        }
        return result;
    }



    public Map<String, Object> getOwnerStats() {
            // 1. & 2. Get User Identity from Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // email from Auth principal (we have saved in OTP controller)
        String email = auth.getName();

        // 3. Fetch user (safe to use Email ignore case)
        Users user = usersRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found for: " + email));

        // 4. Get owner
        Owner owner = user.getOwner();
        if (owner == null) {
            throw new RuntimeException("Owner not found");
        }

        Long ownerId = owner.getId();

        int totalPgs = pgRepository.countPgsByOwnerId(ownerId);
        int totalRooms = roomRepository.countRoomsByOwnerId(ownerId);
        int totalTenants = tenantRepository.countTenantsByOwnerId(ownerId);
        int totalMeters = meterRepository.countMetersByOwnerId(ownerId);
        List<PG> pgs = pgRepository.findByOwnerId(ownerId);

        Map<String, Object> response = new HashMap<>();
        response.put("ownerId",ownerId);
        response.put("totalPgs", totalPgs);
        response.put("totalRooms", totalRooms);
        response.put("totalTenants", totalTenants);
        response.put("totalMeters", totalMeters);
        response.put("ownerName", owner.getName());
        response.put("ownerMobile", owner.getMobile());
        response.put("ownerEmail", email);
        response.put("ownerPGs", pgs);

        return response;
    }


    public List<PG> getAllPgs(Long ownerId) {
        return pgRepository.findByOwnerId(ownerId);
    }
}
