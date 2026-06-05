package com.flatshareteam.flatsharebackend.listings.repository;

import com.flatshareteam.flatsharebackend.listings.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
}
