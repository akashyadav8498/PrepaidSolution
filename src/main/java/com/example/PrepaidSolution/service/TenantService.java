package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.model.Owner;
import com.example.PrepaidSolution.model.Tenant;
import com.example.PrepaidSolution.model.Users;
import com.example.PrepaidSolution.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantService{

    @Autowired
    UsersRepository usersRepository;

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
}
