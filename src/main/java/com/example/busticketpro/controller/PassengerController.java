package com.example.busticketpro.controller;

import com.example.busticketpro.dto.BookingRequestDTO;
import com.example.busticketpro.model.*;
import com.example.busticketpro.repository.*;
import com.example.busticketpro.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PassengerController {

    @Autowired private TripService tripService;
    @Autowired private SeatService seatService;
    @Autowired private SeatRepository seatRepository;
    @Autowired private BookingService bookingService;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/passenger/trips")
    public String listTripsForPassenger(Model model, Authentication auth) {
        List<Trip> trips = tripService.getUpcomingTrips();
        model.addAttribute("trips", trips);
        model.addAttribute("username", auth.getName());
        return "passenger_trips";
    }

    @GetMapping("/passenger/trips/{tripId}/seats")
    public String seatMap(@PathVariable Long tripId, Model model, Authentication auth) {
        Trip trip = tripService.getById(tripId);
        List<Seat> seats = seatService.getSeatsForTrip(tripId);
        model.addAttribute("trip", trip);
        model.addAttribute("seats", seats);
        model.addAttribute("username", auth.getName());
        return "seat_map";
    }

    @GetMapping("/booking-form")
    public String bookingForm(@RequestParam Long tripId,
                              @RequestParam Long seatId,
                              Model model, Authentication auth) {
        Trip trip = tripService.getById(tripId);
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế"));
        if (seat.getStatus() != SeatStatus.AVAILABLE)
            return "redirect:/passenger/trips/" + tripId + "/seats?error=seat_taken";
        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setTripId(tripId);
        dto.setSeatId(seatId);
        model.addAttribute("bookingRequest", dto);
        model.addAttribute("trip", trip);
        model.addAttribute("seat", seat);
        model.addAttribute("username", auth.getName());
        return "booking_form";
    }

    @PostMapping("/book-ticket")
    public String bookTicket(@Valid @ModelAttribute("bookingRequest") BookingRequestDTO dto,
                             BindingResult bindingResult,
                             Model model, Authentication auth,
                             RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            Trip trip = tripService.getById(dto.getTripId());
            Seat seat = seatRepository.findById(dto.getSeatId()).orElse(null);
            model.addAttribute("trip", trip);
            model.addAttribute("seat", seat);
            model.addAttribute("username", auth.getName());
            return "booking_form";
        }
        try {
            Ticket ticket = bookingService.processBooking(dto, auth.getName());
            redirectAttrs.addFlashAttribute("successMsg", "Đặt vé thành công! Mã vé: " + ticket.getTicketCode());
            return "redirect:/booking-success";
        } catch (RuntimeException e) {
            Trip trip = tripService.getById(dto.getTripId());
            Seat seat = seatRepository.findById(dto.getSeatId()).orElse(null);
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("trip", trip);
            model.addAttribute("seat", seat);
            model.addAttribute("bookingRequest", dto);
            model.addAttribute("username", auth.getName());
            return "booking_form";
        }
    }

    @GetMapping("/booking-success")
    public String bookingSuccess(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        return "booking_success";
    }

    @GetMapping("/ticket-lookup")
    public String lookupTicket(@RequestParam(required = false) String ticketCode,
                               @RequestParam(required = false) String phone,
                               Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        if (ticketCode == null || phone == null) return "ticket_lookup";
        return ticketRepository.findByTicketCodeAndPhone(ticketCode, phone)
                .map(ticket -> { model.addAttribute("ticket", ticket); return "ticket_lookup"; })
                .orElseGet(() -> { model.addAttribute("error", "Không tìm thấy vé."); return "ticket_lookup"; });
    }

    // CORE-09: Xem danh sách vé
    @GetMapping("/passenger/my-tickets")
    public String myTickets(Model model, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        List<Ticket> tickets = ticketRepository.findByUserId(user.getId());
        model.addAttribute("tickets", tickets);
        model.addAttribute("username", auth.getName());
        return "my_tickets";
    }

    // CORE-09: Hủy vé
    @PostMapping("/passenger/cancel-ticket/{id}")
    public String cancelTicket(@PathVariable Long id,
                               Authentication auth,
                               RedirectAttributes redirectAttrs) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Ticket ticket = ticketRepository.findByIdWithTrip(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));
        

        if (!ticket.getUser().getId().equals(user.getId())) {
            redirectAttrs.addFlashAttribute("error", "Bạn không có quyền hủy vé này!");
            return "redirect:/passenger/my-tickets";
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            redirectAttrs.addFlashAttribute("error", "Vé này đã bị hủy trước đó!");
            return "redirect:/passenger/my-tickets";
        }
        if (ticket.getStatus() == TicketStatus.PAID) {
            redirectAttrs.addFlashAttribute("error", "Vé đã thanh toán không thể hủy!");
            return "redirect:/passenger/my-tickets";
        }

        LocalDateTime departureTime = ticket.getTrip().getDepartureTime();
        if (LocalDateTime.now().isAfter(departureTime.minusHours(12))) {
            redirectAttrs.addFlashAttribute("error", "Chỉ được hủy vé trước 12 tiếng so với giờ khởi hành!");
            return "redirect:/passenger/my-tickets";
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        Seat seat = ticket.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setLockedAt(null);
        seatRepository.save(seat);

        redirectAttrs.addFlashAttribute("success", "Hủy vé thành công! Mã vé: " + ticket.getTicketCode());
        return "redirect:/passenger/my-tickets";
    }
}