package com.example.be.repository;

import com.example.be.domain.RefreshToken;
import com.example.be.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Long> {
    Room findByTitle(String title);
}
