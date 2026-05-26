package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.model.LiveMeterReadings;
import com.example.PrepaidSolution.model.Owner;
import com.example.PrepaidSolution.model.Tenant;
import com.example.PrepaidSolution.model.Users;
import com.example.PrepaidSolution.repository.LiveMeterReadingsRepository;
import com.example.PrepaidSolution.repository.UsersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.type.descriptor.java.ObjectJavaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantService{

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    private LiveMeterReadingsRepository liveMeterReadingsRepository;

    public Map<String, Object> getTenantData(String email){

        // 3. Fetch user (safe to use Email ignore case)
        Users user = usersRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found for: " + email));

        // 4. Get Tenant
        Tenant tenant = user.getTenant();
        if (tenant == null) {
            throw new RuntimeException("Tenant not found");
        }

        Long tenantId = tenant.getId();

        String tenantName = tenant.getName();
        String phoneNo = tenant.getMobile();

        String roomNo = null;
        if(tenant.getRoom() != null){
            roomNo = tenant.getRoom().getRoomNo();
        }


        double currentBalance = 0;
        if(tenant.getBalance() != null){
            currentBalance = tenant.getBalance().getCurrentBalance();
        }

        String meterSerialNo = null;
        if(tenant.getRoom() != null && tenant.getRoom().getMeter() != null){
            meterSerialNo = tenant.getRoom().getMeter().getSerialNo();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId);
        response.put("tenantName", tenantName);
        response.put("tenantEmail", email);
        response.put("tenantPhoneNumber", phoneNo);
        response.put("tenantRoomNumber", roomNo);
        response.put("tenantMeterSerialNumber", meterSerialNo);
        response.put("tenantCurrentBalance", currentBalance);

        return response;
    }

    public Map<String, Object> getLatestMeterReading(String meterSerialNo) throws JsonProcessingException {

        Map<String, Object> response = new HashMap<>();

        String meterId = "N/A";
        boolean relayStatus = false;
        boolean connectionStatus = false;
        double readingId = 0;
        double todayEbUsage = 0;
        double todayDgUsage = 0;
        double totalEbReading = 0;
        double totalDgReading = 0;
        LiveMeterReadings previousDayLastReading = null ;

        LiveMeterReadings reading =
                liveMeterReadingsRepository.findTopByMeterIdOrderByCreatedAtDesc(meterSerialNo);

        meterId = reading.getMeterId();
        readingId = reading.getId();

                ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(reading.getReading());

        JsonNode liveData = root.get("live_data");


        if (reading != null && reading.getReading() != null) {

            try {

                if (liveData != null) {

                    if (liveData.has("rls")) {
                        int rls = liveData.get("rls").asInt();
                        relayStatus = ((rls & 3) != 0); // (bitwise)
                    }

                    // (rls & 3) != 0
                    //If last 2 bits are anything except 00 → TRUE
                    //If last 2 bits are 00 → FALSE

                    if (liveData.has("kwh1")) {
                        totalEbReading = liveData.get("kwh1").asDouble();
                    }

                    if (liveData.has("kwh2")) {
                        totalDgReading = liveData.get("kwh2").asDouble();
                    }
                }



                // =========================
                // TODAY USAGE CALCULATION
                // =========================


                // latest reading ki date
                LocalDate readingDate =
                        reading.getCreatedAt().toLocalDate();


                // same date ka midnight
                LocalDateTime startOfDay =
                        readingDate.atStartOfDay();


                // yesterday ki last reading
                previousDayLastReading =
                        liveMeterReadingsRepository
                                .findTopByMeterIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                                        meterSerialNo,
                                        startOfDay
                                );


                if (previousDayLastReading != null &&
                        previousDayLastReading.getReading() != null) {

                    JsonNode previousRoot =
                            mapper.readTree(
                                    previousDayLastReading.getReading()
                            );

                    JsonNode previousLiveData =
                            previousRoot.get("live_data");


                    double previousEb = 0;
                    double previousDg = 0;


                    if (previousLiveData != null) {

                        if (previousLiveData.has("kwh1")) {

                            previousEb =
                                    previousLiveData
                                            .get("kwh1")
                                            .asDouble();
                        }

                        if (previousLiveData.has("kwh2")) {

                            previousDg =
                                    previousLiveData
                                            .get("kwh2")
                                            .asDouble();
                        }
                    }


                    // FINAL TODAY USAGE

                    todayEbUsage = totalEbReading - previousEb;

                    todayDgUsage = totalDgReading - previousDg;

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

                response.put("readingId",readingId);
                response.put("meterSerialNo",meterId);
                response.put("lastReading", liveData);
                response.put("yesterdayLastReading",previousDayLastReading);
                response.put("relayStatus", relayStatus);
                response.put("connectionStatus", connectionStatus);
                response.put("totalEbReading", totalEbReading);
                response.put("totalDgReading", totalDgReading);
                response.put("todayEbUsage", todayEbUsage);
                response.put("todayDgUsage", todayDgUsage);
                response.put("lastUpdatedTime", reading.getCreatedAt());

        return response;
    }
}
