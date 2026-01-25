package com.example.be.repository;

import com.example.be.domain.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room,Long> {
    Room findByTitle(String title);

    /**
     * 비관적 락을 사용하여 Room 조회
     * 동시에 여러 사용자가 입장할 때 참여자 수 정합성 보장
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.title = :title")
    Room findByTitleWithLock(@Param("title") String title);
}
