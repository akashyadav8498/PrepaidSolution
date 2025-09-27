package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.model.MeterType;
import com.example.PrepaidSolution.model.Owner;
import com.example.PrepaidSolution.model.PG;
import com.example.PrepaidSolution.repository.MeterTypeRepository;
import com.example.PrepaidSolution.repository.OwnerRepository;
import com.example.PrepaidSolution.repository.PGRepository;
import com.example.PrepaidSolution.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MVCController {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PGRepository pgRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MeterTypeRepository meterTypeRepository;

    @GetMapping("/meter_management")
    public String getAdmin(Model model) {
        List<Owner> owners = ownerRepository.findAll();
        List<PG> pgs = pgRepository.findAll();
        List<MeterType> meterTypes = meterTypeRepository.findAll();

        model.addAttribute("owners", owners);
        model.addAttribute("pgs", pgs);
        model.addAttribute("metertypes", meterTypes);
        return "meter_management";
    }

    @GetMapping("/meter_management_pwa")
    public String getPWAAdmin() {
        return "forward:/pwa/meter_management_mobile.html";
    }

}
