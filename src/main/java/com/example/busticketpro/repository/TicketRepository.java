package com.example.busticketpro.repository;

import com.example.busticketpro.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.busticketpro.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findBySeatIdAndStatus(Long seatId, TicketStatus status);
    Optional<Ticket> findByTicketCodeAndPhone(String ticketCode, String phone);
    //Tìm danh sách tất cả các vé thuộc về một chuyến xe cụ thể.
    List<Ticket> findByTripId(Long tripId);

    /**
     * Chức năng: Lấy danh sách vé theo trạng thái (như PENDING, PAID) và tự động tải kèm
     * tất cả thông tin liên quan như Chuyến, Tuyến đường, Địa điểm và Xe.
     */
    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.trip tr " +
            "JOIN FETCH tr.route r " +
            "JOIN FETCH r.fromLocation " +
            "JOIN FETCH r.toLocation " +
            "JOIN FETCH tr.bus " +
            "JOIN FETCH t.seat " +
            "WHERE t.status = :status")
    List<Ticket> findByStatusWithDetails(@Param("status") TicketStatus status);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.trip tr " +
            "JOIN FETCH tr.route r " +
            "JOIN FETCH r.fromLocation " +
            "JOIN FETCH r.toLocation " +
            "JOIN FETCH tr.bus " +
            "JOIN FETCH t.seat " +
            "WHERE t.user.id = :userId " +
            "ORDER BY t.bookedAt ASC")
    List<Ticket> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.trip tr " +
            "JOIN FETCH t.seat " +
            "WHERE t.id = :id")
    Optional<Ticket> findByIdWithTrip(@Param("id") Long id);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.trip tr " +
            "JOIN FETCH tr.route r " +
            "JOIN FETCH r.fromLocation " +
            "JOIN FETCH r.toLocation " +
            "JOIN FETCH tr.bus " +
            "JOIN FETCH t.seat " +
            "WHERE t.ticketCode = :ticketCode AND t.phone = :phone")
    Optional<Ticket> findByTicketCodeAndPhoneWithDetails(@Param("ticketCode") String ticketCode,
                                                         @Param("phone") String phone);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.seat " +
            "JOIN FETCH t.trip tr " +
            "JOIN FETCH tr.route r " +
            "JOIN FETCH r.fromLocation " +
            "JOIN FETCH r.toLocation " +
            "JOIN FETCH tr.bus " +
            "WHERE t.status = 'PENDING' AND t.bookedAt < :expiredTime")
    List<Ticket> findExpiredPendingTickets(@Param("expiredTime") LocalDateTime expiredTime);

    @Query(
            value = "SELECT t FROM Ticket t " +
                    "JOIN FETCH t.trip tr " +
                    "JOIN FETCH tr.route r " +
                    "JOIN FETCH r.fromLocation " +
                    "JOIN FETCH r.toLocation " +
                    "JOIN FETCH tr.bus " +
                    "JOIN FETCH t.seat " +
                    "WHERE t.user.id = :userId",
            countQuery = "SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId"
    )
    Page<Ticket> findByUserIdWithDetailsPage(@Param("userId") Long userId, Pageable pageable);
}