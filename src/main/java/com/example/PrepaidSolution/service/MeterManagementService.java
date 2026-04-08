package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.dto.PGDropdownDTO;
import com.example.PrepaidSolution.model.*;
import com.example.PrepaidSolution.repository.*;
import com.example.PrepaidSolution.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MeterManagementService {

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
    private BalanceRepository balanceRepository;

    public Meter saveMeter(Meter meter, Long roomId) {

        //  Get Room from DB
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        //  Set relation
        meter.setRoom(room);

        //  Default value (optional)
        if (meter.getIsActive() == null) {
            meter.setIsActive(true);
        }

        // Save meter
        return meterRepository.save(meter);
    }


    public List<Room> getRoomsByPg(Long pgId) {
        return roomRepository.findByPgId(pgId);
    }

    public List<PGDropdownDTO> getPgsByOwner(Long ownerId) {

        return pgRepository.findAllByOwner_Id(ownerId)
                .stream()
                .map(pg -> new PGDropdownDTO(
                        pg.getId(),
                        pg.getName()
                ))
                .toList();
    }

    public Map<String,Object> getAllUsers() {
        Map<String,Object> result = new HashMap<>();
        List<Users> users = usersRepository.findAll();

        result.put("users", users);
        return result;
    }

    public Map<String, Long> getDashboardCounts() {

        long totalPG = pgRepository.count();
        long totalOwners = usersRepository.countByRole(Users.Role.OWNER);
        long totalTenants = usersRepository.countByRole(Users.Role.TENANT);
        long totalMeters = meterRepository.count();
        long totalRooms = roomRepository.count();

        Map<String, Long> result = new HashMap<>();

        result.put("totalPG", totalPG);
        result.put("totalOwners", totalOwners);
        result.put("totalTenants", totalTenants);
        result.put("totalMeters", totalMeters);
        result.put("totalRooms", totalRooms);

        return result;
    }


    public ResponseEntity<?> getMeterReadings(String meterId){
        try{
            List<LiveMeterReadings> data =
                    liveMeterReadingsRepository.findAllByMeterId(meterId, Sort.by(Sort.Direction.DESC, "id"));

            if (data.isEmpty()){
                return ResponseEntity.status(404).body("No readings found for meterId: " + meterId);
            }else{
                return ResponseEntity.ok(data);
            }
        }catch(Exception e){
            return ResponseEntity.status(500).body("Error fetching meter readings: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getOnloadData(HttpServletRequest httpServletRequest) {
        try {
            HttpSession session = httpServletRequest.getSession(false);
            //String userName = (String) session.getAttribute("userName");
            //String userRole = (String) session.getAttribute("userRole");
            List<LiveMeterReadings> liveReadings =
                    liveMeterReadingsRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));



//            List<Map<String, Object>> parsedList = liveReadings.stream()
//                    .map(reading -> {
//
//                        Map<String, Object> map = new LinkedHashMap<>();
//
//                        // Copy original fields
//                        map.put("id", reading.getId());
//                        map.put("createdAt", reading.getCreatedAt());
//
//                        // Parse reading JSON string into real JSON object
//                        JsonObject jsonObject = JsonParser
//                                .parseString(reading.getReading())
//                                .getAsJsonObject();
//
//                        map.put("reading", jsonObject);
//
//                        return map;
//                    })
//                    .toList();
//
//            return ResponseEntity.ok(parsedList);

            return ResponseEntity.ok(liveReadings);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Error while fetching live readings"), HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

//    public ResponseEntity<?> getOnloadData(HttpServletRequest httpServletRequest) {
//        try {
//            HttpSession session = httpServletRequest.getSession(false);
//            //String userName = (String) session.getAttribute("userName");
//            //String userRole = (String) session.getAttribute("userRole");
//            List<LiveMeterReadings> liveReadings =
//                    liveMeterReadingsRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
//
//
//            List<Map<String, Object>> parsedList = liveReadings.stream()
//                .map(reading -> {
//
//                    Map<String, Object> map = new LinkedHashMap<>();
//
//                    // Copy original fields
//                    map.put("id", reading.getId());
//                    map.put("createdAt", reading.getCreatedAt());
//
//                    // Parse reading JSON string into real JSON object
//                    JsonObject jsonObject = JsonParser
//                            .parseString(reading.getReading())
//                            .getAsJsonObject();
//
//                    map.put("reading", jsonObject);
//
//                    return map;
//                })
//                .toList();
//
//        return ResponseEntity.ok(parsedList);
//        } catch (Exception e) {
//            return new ResponseEntity<>(Map.of("message", "Error while fetching live readings"), HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
//        }
//    }

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

        Users user = new Users();
        user.setUsername(username);
        user.setPassword(Utility.passwordEncoder.encode(password));
        user.setRole(Users.Role.OWNER);
        user.setEmail(ownerEmail); // <- this line
        Users savedUser = usersRepository.save(user);

        Owner owner = new Owner();
        owner.setUsers(savedUser);
        owner.setName(ownerName);
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
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
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

        Room room = roomRepository.findById(tenantRoom)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStatus() == Room.Status.OCCUPIED) {
            throw new RuntimeException("Room already occupied");
        }

        // ============================
        // ✅ Create User
        // ============================
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(Utility.passwordEncoder.encode(password));
        user.setRole(Users.Role.TENANT);
        user.setEmail(tenantEmail); // <--

        Users savedUser = usersRepository.save(user);

        // ============================
        // ✅ Create Tenant
        // ============================
        Tenant tenant = new Tenant();
        tenant.setMobile(tenantMobile);
        tenant.setUsers(savedUser);
        tenant.setName(tenantName);
        tenant.setAddress(tenantAddress);
        tenant.setRoom(room);

        Tenant savedTenant = tenantRepository.save(tenant);

        // ============================
        // ✅ Create Balance
        // ============================
        Balance balance = new Balance();
        balance.setTenant(savedTenant); // ✅ ONLY THIS
        balance.setCurrentBalance(0.0);
        balance.setUpdatedAt(LocalDateTime.now());

        balanceRepository.save(balance);

        // ============================
        // ✅ Update Room Status
        // ============================
        room.setStatus(Room.Status.OCCUPIED);
        roomRepository.save(room);

        return Map.of(
                "username", username,
                "password", password,
                "name", tenantName,
                "email", tenantEmail
        );
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
