package com.example.busticketpro.service;

import com.example.busticketpro.model.*;
import com.example.busticketpro.repository.SeatRepository;
import com.example.busticketpro.repository.TicketRepository;
import com.example.busticketpro.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private TicketRepository ticketRepository;

    // Lấy danh sách ghế của chuyến xe, tự tạo nếu chưa có
    @Transactional
    public List<Seat> getSeatsForTrip(Long tripId) {
        List<Seat> seats = seatRepository.findByTripId(tripId);
        if (seats.isEmpty()) {
            // Tự động tạo ghế dựa theo số ghế của xe
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe"));
            int seatCount = trip.getBus().getSeatCount();
            for (int i = 1; i <= seatCount; i++) {
                Seat seat = new Seat();
                seat.setTrip(trip);
                seat.setSeatNumber(String.format("%02d", i)); // 01, 02, ...
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
            seats = seatRepository.findByTripId(tripId);
        }
        return seats;
    }

    //  Giữ chỗ tạm thời 15 phút
    @Transactional
    public Seat lockSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế"));
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new RuntimeException("Ghế đã được đặt hoặc đang được giữ");
        }
        seat.setStatus(SeatStatus.PENDING);
        seat.setLockedAt(LocalDateTime.now());
        return seatRepository.save(seat);
    }

    // Tự động giải phóng ghế quá 15 phút mỗi 1 phút
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredSeats() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(15);

        // Tìm các vé PENDING quá hạn và hủy luôn
        List<Seat> expiredSeats = seatRepository.findExpiredPendingSeats(expiredTime);
        for (Seat seat : expiredSeats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedAt(null);
            seatRepository.save(seat);

            //  Cập nhật vé sang CANCELLED
            ticketRepository.findBySeatIdAndStatus(seat.getId(), TicketStatus.PENDING)
                    .ifPresent(ticket -> {
                        ticket.setStatus(TicketStatus.CANCELLED);
                        ticketRepository.save(ticket);
                    });
        }
        System.out.println(" Đã giải phóng ghế hết hạn giữ chỗ");
    }

    @Scheduled(fixedRate = 60000) // mỗi 1 phút
    @Transactional
    public void releaseSeatsPastTrips() {
        LocalDateTime now = LocalDateTime.now();
        List<Seat> seats = seatRepository.findByTripDepartureBeforeAndStatus(now, SeatStatus.PENDING);
        for (Seat s : seats) {
            s.setStatus(SeatStatus.AVAILABLE);
            s.setLockedAt(null);
            seatRepository.save(s);
        }
        System.out.println("Giải phóng ghế PENDING cho các chuyến đã đi");
    }
}