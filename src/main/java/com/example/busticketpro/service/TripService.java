package com.example.busticketpro.service;

import com.example.busticketpro.dto.TripDTO;
import com.example.busticketpro.model.*;
import com.example.busticketpro.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripService {

    @Autowired private TripRepository tripRepository;
    @Autowired private BusRepository busRepo;
    @Autowired private RouteRepository routeRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private SeatRepository seatRepository;

    // ── Lấy tất cả chuyến (dành cho admin)
    public List<Trip> getAllTripsForAdmin() {
        return tripRepository.findAll();
    }

    public List<Trip> getUpcomingTrips() {
        LocalDateTime now = LocalDateTime.now();
        return tripRepository.findAll()
                .stream()
                .filter(trip -> trip.getDepartureTime().isAfter(now))
                .toList();
    }

    // ── Tạo chuyến mới
    @Transactional
    public Trip createTrip(TripDTO dto) {
        Bus bus = busRepo.findById(dto.getBusId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tuyến đường"));

        Trip trip = new Trip();
        trip.setBus(bus);
        trip.setRoute(route);
        trip.setDepartureTime(dto.getDepartureTime());
        trip.setPrice(dto.getPrice());
        return tripRepository.save(trip);
    }

    // ── Lấy chuyến theo ID
    public Trip getById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe"));
    }

    // ── Kiểm tra chuyến đã khởi hành chưa
    public boolean isDeparted(Long tripId) {
        Trip trip = getById(tripId);
        return trip.getDepartureTime().isBefore(LocalDateTime.now());
    }

    // ── Xóa chuyến (Admin)
    // Thứ tự xóa đúng FK: tickets → seats → trip
    // Nếu chuyến chưa khởi hành mà có vé PAID → từ chối xóa
    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = getById(id);
        boolean departed = trip.getDepartureTime().isBefore(LocalDateTime.now());

        List<Ticket> tickets = ticketRepository.findByTripId(id);

        // Chưa khởi hành mà có vé PAID → không cho xóa
        if (!departed) {
            boolean hasPaid = tickets.stream()
                    .anyMatch(t -> t.getStatus() == TicketStatus.PAID);
            if (hasPaid) {
                throw new RuntimeException(
                        "Không thể xóa: Chuyến đang có vé đã thanh toán. " +
                                "Chỉ có thể xóa sau khi chuyến đã khởi hành."
                );
            }
        }

        // Xóa tickets trước (FK từ tickets → seats, trips)
        ticketRepository.deleteAll(tickets);

        // Xóa seats
        List<Seat> seats = seatRepository.findByTripId(id);
        seatRepository.deleteAll(seats);

        // Cuối cùng xóa trip
        tripRepository.delete(trip);
    }

    // ── Giải phóng ghế cho các chuyến đã khởi hành (dành cho cronjob)
    @Transactional
    public void releaseSeatsForDepartedTrips() {
        LocalDateTime now = LocalDateTime.now();
        List<Trip> trips = tripRepository.findAll();
        for (Trip trip : trips) {
            if (trip.getDepartureTime().isBefore(now)) {
                List<Seat> seats = seatRepository.findByTripId(trip.getId());
                for (Seat seat : seats) {
                    if (seat.getStatus() != SeatStatus.AVAILABLE) {
                        seat.setStatus(SeatStatus.AVAILABLE);
                        seat.setLockedAt(null);
                        seatRepository.save(seat);
                    }
                }
            }
        }
    }
}