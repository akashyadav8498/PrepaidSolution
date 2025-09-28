package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.model.*;
import com.example.PrepaidSolution.repository.*;
import com.example.PrepaidSolution.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MeterManagementService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MeterReadingsRepository meterReadingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PGRepository pgRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EmailService emailService;

    public ResponseEntity<?> getOnloadData(HttpServletRequest httpServletRequest) {
        try {
            HttpSession session = httpServletRequest.getSession(false);
            String userName = (String) session.getAttribute("userName");
            String userRole = (String) session.getAttribute("userRole");
            List<MeterReadings> liveReadings = meterReadingsRepository.findAll();
            return new ResponseEntity<>(Map.of("liveReadings", liveReadings), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Error while fetching live readings"), HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    public ResponseEntity<?> addOwner(Map<String, String> requestMap) {
        try {
            Map<String, String> ownerDetails = saveOwner(requestMap);
            emailService.sendCredentialsEmail(ownerDetails);
            return new ResponseEntity<>(Map.of("message", "Owner added successfully"), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Owner could not be added"), HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Transactional
    public Map<String, String> saveOwner(Map<String, String> requestMap) {
        String ownerName = requestMap.get("ownerName");
        String ownerMobile = requestMap.get("ownerMobile");
        String ownerEmail = requestMap.get("ownerEmail");
        String ownerAddress = requestMap.get("ownerAddress");

        String username = Utility.generateUsername(ownerName, ownerMobile, 7);
        String password = Utility.generatePassword(7);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.OWNER);
        User savedUser = userRepository.save(user);

        Owner owner = new Owner();
        owner.setUser(savedUser);
        owner.setName(ownerName);
        owner.setEmail(ownerEmail);
        owner.setAddress(ownerAddress);
        owner.setMobile(ownerMobile);
        ownerRepository.save(owner);

        return Map.of("username", username, "password", password, "name", ownerName, "email", ownerEmail);
    }

    public ResponseEntity<?> addPG(Map<String, String> requestMap) {
        try {
            savePG(requestMap);
            return new ResponseEntity<>(Map.of("message", "PG added successfully"), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "PG could not be added"), HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Transactional
    public void savePG(Map<String, String> requestMap) {
        String pgName = requestMap.get("pgName");
        String pgOwnerId = requestMap.get("pgOwnerId");
        String pgAddress = requestMap.get("pgAddress");

        Owner owner = ownerRepository.findById(Long.valueOf(pgOwnerId)).orElse(null);

        PG pg = new PG();
        pg.setOwner(owner);
        pg.setName(pgName);
        pg.setAddress(pgAddress);
        pgRepository.save(pg);
    }

    public ResponseEntity<?> addRoom(Map<String, String> requestMap) {
        try {
            saveRoom(requestMap);
            return new ResponseEntity<>(Map.of("message", "Room added successfully"), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Room could not be added"), HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Transactional
    public void saveRoom(Map<String, String> requestMap) {
        String roomNumber = requestMap.get("roomNumber");
        String pgId = requestMap.get("roomPG");

        PG pg = pgRepository.findById(Long.valueOf(pgId)).get();

        Room room = new Room();
        room.setPg(pg);
        room.setRoomNo(roomNumber);
        room.setStatus(Room.Status.VACANT);
        roomRepository.save(room);
    }

    public ResponseEntity<?> addTenant(Map<String, String> requestMap) {
        try {
            Map<String, String> tenantDetails = saveTenant(requestMap);
            emailService.sendCredentialsEmail(tenantDetails);
            return new ResponseEntity<>(Map.of("message", "Tenant added successfully"), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Tenant could not be added"), HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Transactional
    public Map<String, String> saveTenant(Map<String, String> requestMap) {
        Long tenantRoom = Long.valueOf(requestMap.get("tenantRoom"));
        String tenantName = requestMap.get("tenantName");
        String tenantEmail = requestMap.get("tenantEmail");
        String tenantMobile = requestMap.get("tenantMobile");
        String tenantAddress = requestMap.get("tenantAddress");

        String username = Utility.generateUsername(tenantName, tenantMobile, 7);
        String password = Utility.generatePassword(7);

        Room room = roomRepository.findById(tenantRoom).orElse(null);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.TENANT);
        User savedUser = userRepository.save(user);

        Tenant tenant = new Tenant();
        tenant.setUser(savedUser);
        tenant.setName(tenantName);
        tenant.setEmail(tenantEmail);
        tenant.setAddress(tenantAddress);
        tenant.setRoom(room);
        tenantRepository.save(tenant);

        return Map.of("username", username, "password", password, "name", tenantName, "email", tenantEmail);
    }

    public ResponseEntity<?> getRooms(String pgId) {
        List<Room> rooms = roomRepository.findAllByPgIdEqualsAndStatus(Long.valueOf(pgId), Room.Status.VACANT);
        List<Map<String, String>> roomList = rooms.stream()
                .map(room -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", String.valueOf(room.getId()));
                    map.put("roomNo", room.getRoomNo());
                    return map;
                })
                .toList();
        return new ResponseEntity<>(Map.of("rooms", roomList), HttpStatusCode.valueOf(HttpStatus.OK.value()));
    }
}
