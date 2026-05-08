package com.example.busticketpro.repository;

import com.example.busticketpro.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.busticketpro.model.TicketStatus;
import java.util.List;
import java.util.Optional;
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Optional<Ticket> findBySeatIdAndStatus(Long seatId, TicketStatus status);
    Optional<Ticket> findByTicketCodeAndPhone(String ticketCode, String phone);
    List<Ticket> findByTripId(Long tripId);
    //  JOIN FETCH để load đầy đủ dữ liệu liên quan
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
            "WHERE t.user.id = :userId")
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
}