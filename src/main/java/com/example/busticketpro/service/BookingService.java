package com.example.busticketpro.service;

import com.example.busticketpro.dto.BookingRequestDTO;
import com.example.busticketpro.model.*;
import com.example.busticketpro.repository.SeatRepository;
import com.example.busticketpro.repository.TicketRepository;
import com.example.busticketpro.repository.TripRepository;
import com.example.busticketpro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class BookingService {

    @Autowired private SeatRepository seatRepository;
    @Autowired private TripRepository tripRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Ticket processBooking(BookingRequestDTO dto, String username) {

        // Lock seat
        Seat seat = seatRepository.findByIdWithLock(dto.getSeatId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế"));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new RuntimeException("Ghế " + seat.getSeatNumber() + " đã được đặt.");
        }

        Trip trip = tripRepository.findById(dto.getTripId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe"));

        if (!seat.getTrip().getId().equals(trip.getId())) {
            throw new RuntimeException("Ghế không thuộc chuyến xe này");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Tạo ticket
        Ticket ticket = new Ticket();
        ticket.setTicketCode(generateTicketCode());
        ticket.setTrip(trip);
        ticket.setSeat(seat);
        ticket.setUser(user);
        ticket.setPassengerName(dto.getPassengerName());
        ticket.setPhone(dto.getPhone());
        ticket.setEmail(dto.getEmail());
        ticket.setTotalPrice(BigDecimal.valueOf(trip.getPrice()));
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setBookedAt(LocalDateTime.now());

        ticketRepository.save(ticket);

        // Update seat
        seat.setStatus(SeatStatus.PENDING);
        seat.setLockedAt(LocalDateTime.now());
        seatRepository.save(seat);

        return ticket;
    }

    private String generateTicketCode() {
        String date   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%04d", new Random().nextInt(9000) + 1000);
        return "BP-" + date + "-" + suffix;
    }
}