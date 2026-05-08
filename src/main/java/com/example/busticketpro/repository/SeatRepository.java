package com.example.busticketpro.repository;

import com.example.busticketpro.model.Seat;
import com.example.busticketpro.model.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Đã có trong code cũ của bạn - giữ nguyên
    List<Seat> findByTripId(Long tripId);

    @Modifying
    @Query("UPDATE Seat s SET s.status = com.example.busticketpro.model.SeatStatus.AVAILABLE, s.lockedAt = null " +
            "WHERE s.status = com.example.busticketpro.model.SeatStatus.PENDING " +
            "AND s.lockedAt < :expiredTime")
    void releaseExpiredSeats(@Param("expiredTime") LocalDateTime expiredTime);

    // MỚI: SELECT FOR UPDATE - lock row để tránh race condition (CORE-06)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT s FROM Seat s WHERE s.trip.departureTime < :now AND s.status = :status")
    List<Seat> findByTripDepartureBeforeAndStatus(@Param("now") LocalDateTime now, @Param("status") SeatStatus status);

    @Query("SELECT s FROM Seat s WHERE s.status = 'PENDING' AND s.lockedAt < :expiredTime")
    List<Seat> findExpiredPendingSeats(@Param("expiredTime") LocalDateTime expiredTime);
}