package com.example.busticketpro.service;

import com.example.busticketpro.dto.BookingRequestDTO;
import com.example.busticketpro.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BookingService {

    @Autowired private SeatService seatService;
    @Autowired private EmailService emailService;
    @Autowired private TripRepository tripRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<Ticket> processBooking(BookingRequestDTO dto, String username) {

        // 1. Tìm chuyến xe
        Trip trip = tripRepository.findById(dto.getTripId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe"));

        // 2. Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<Ticket> tickets = new ArrayList<>();

        // 3. Lặp qua từng ghế
        for (Long seatId : dto.getSeatIds()) {

            // Lock ghế - nếu ghế đã bị đặt sẽ throw exception → rollback toàn bộ
            Seat seat = seatService.lockSeat(seatId);

            if (!seat.getTrip().getId().equals(trip.getId())) {
                throw new RuntimeException("Ghế không thuộc chuyến xe này");
            }

            // Tạo ticket cho từng ghế
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

            Ticket saved = ticketRepository.save(ticket);
            tickets.add(saved);
        }

        // 4. Gửi email xác nhận cho từng vé sau khi tất cả đã lưu thành công
        tickets.forEach(t -> {
            if (t.getEmail() != null && !t.getEmail().isEmpty()) {
                emailService.sendBookingConfirmation(t);
            }
        });

        return tickets;
    }

    private String generateTicketCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%04d", new Random().nextInt(9000) + 1000);
        return "BP-" + date + "-" + suffix;
    }
}