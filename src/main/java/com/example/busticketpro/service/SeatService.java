package com.example.busticketpro.service;

import com.example.busticketpro.model.*;
import com.example.busticketpro.repository.SeatRepository;
import com.example.busticketpro.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatService {

    @Autowired private SeatRepository seatRepository;
    @Autowired private TripRepository tripRepository;

    @Transactional
    public List<Seat> getSeatsForTrip(Long tripId) {
        List<Seat> seats = seatRepository.findByTripId(tripId);
        if (seats.isEmpty()) {
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe"));
            int seatCount = trip.getBus().getSeatCount();
            for (int i = 1; i <= seatCount; i++) {
                Seat seat = new Seat();
                seat.setTrip(trip);
                seat.setSeatNumber(String.format("%02d", i));
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
            seats = seatRepository.findByTripId(tripId);
        }
        return seats;
    }

    // Chạy trong transaction của BookingService - SELECT FOR UPDATE có hiệu lực
    @Transactional(propagation = Propagation.MANDATORY)
    public Seat lockSeat(Long seatId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế"));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new RuntimeException("Ghế " + seat.getSeatNumber() + " đã được đặt hoặc đang được giữ");
        }

        seat.setStatus(SeatStatus.PENDING);
        seat.setLockedAt(LocalDateTime.now());
        return seatRepository.save(seat);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseSeatsPastTrips() {
        LocalDateTime now = LocalDateTime.now();
        List<Seat> seats = seatRepository.findByTripDepartureBeforeAndStatus(now, SeatStatus.PENDING);
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedAt(null);
            seatRepository.save(seat);
        }
        System.out.println("Giải phóng ghế PENDING cho các chuyến đã khởi hành");
    }
}