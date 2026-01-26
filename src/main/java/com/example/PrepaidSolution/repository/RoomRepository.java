package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByPgIdEqualsAndStatus(Long pgId, Room.Status status);
}
