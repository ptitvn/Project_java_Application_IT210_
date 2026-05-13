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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PassengerController {

    @Autowired
    private TripService tripService;
    @Autowired
    private SeatService seatService;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    //    Hiển thị danh sách các chuyến xe sắp khởi hành cho hành khách xem.
    @GetMapping("/passenger/trips")
    public String listTripsForPassenger(Model model, Authentication auth) {
        List<Trip> trips = tripService.getUpcomingTrips();
        model.addAttribute("trips", trips);
        model.addAttribute("username", auth.getName());
        return "passenger_trips";
    }

    //    Hiển thị sơ đồ ghế ngồi của một chuyến xe cụ thể để khách chọn chỗ.
    @GetMapping("/passenger/trips/{tripId}/seats")
    public String seatMap(@PathVariable Long tripId, Model model, Authentication auth) {
        Trip trip = tripService.getById(tripId);
        List<Seat> seats = seatService.getSeatsForTrip(tripId);
        model.addAttribute("trip", trip);
        model.addAttribute("seats", seats);
        model.addAttribute("username", auth.getName());
        return "seat_map";
    }

    //    Hiển thị form điền thông tin cá nhân sau khi khách đã chọn được ghế.
    @GetMapping("/booking-form")
    public String bookingForm(@RequestParam Long tripId,
                              @RequestParam List<Long> seatIds,
                              Model model,
                              Authentication auth) {
        Trip trip = tripService.getById(tripId);


        List<Seat> seats = seatIds.stream()
                .map(id -> seatRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế")))
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (seats.isEmpty()) {
            return "redirect:/passenger/trips/" + tripId + "/seats?error=seat_taken";
        }

        Seat seat = seats.get(0);

        model.addAttribute("trip", trip);
        model.addAttribute("seat", seat);
        model.addAttribute("seats", seats);
        model.addAttribute("bookingRequest", new BookingRequestDTO());
        model.addAttribute("username", auth.getName());

        return "booking_form";
    }

    //    Xử lý logic đặt vé, lưu vào database và gửi thông báo thành công.
    @PostMapping("/book-ticket")
    public String bookTicket(@Valid @ModelAttribute("bookingRequest") BookingRequestDTO dto,
                             BindingResult bindingResult,
                             Model model, Authentication auth,
                             RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            Trip trip = tripService.getById(dto.getTripId());
            List<Seat> seats = dto.getSeatIds().stream()
                    .map(id -> seatRepository.findById(id).orElse(null))
                    .collect(Collectors.toList());
            model.addAttribute("trip", trip);
            model.addAttribute("seats", seats);
//        model.addAttribute("bookingRequest", new BookingRequestDTO());
            model.addAttribute("username", auth.getName());
            return "booking_form";
        }
        try {
            List<Ticket> tickets = bookingService.processBooking(dto, auth.getName());
            String codes = tickets.stream()
                    .map(Ticket::getTicketCode)
                    .collect(Collectors.joining(", "));
            redirectAttrs.addFlashAttribute("successMsg", "Đặt vé thành công! Mã vé: " + codes);
            return "redirect:/booking-success";
        } catch (RuntimeException e) {
            // Thông báo rõ ghế nào bị lấy, redirect về seat_map để chọn lại
            redirectAttrs.addFlashAttribute("errorMsg",
                    e.getMessage() + " — Vui lòng chọn lại ghế.");
            return "redirect:/passenger/trips/" + dto.getTripId() + "/seats";
        }
    }

    //    Hiển thị trang thông báo khi hành khách đã đặt vé thành công.
    @GetMapping("/booking-success")
    public String bookingSuccess(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        return "booking_success";
    }

    //    Cho phép hành khách tìm kiếm lại thông tin vé bằng mã vé và số điện thoại.
    @GetMapping("/ticket-lookup")
    public String lookupTicket(@RequestParam(required = false) String ticketCode,
                               @RequestParam(required = false) String phone,
                               Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());

        if (ticketCode == null || phone == null) return "ticket_lookup";

        // Thêm check rỗng
        if (ticketCode.trim().isEmpty() || phone.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập đầy đủ mã vé và số điện thoại.");
            return "ticket_lookup";
        }

        return ticketRepository.findByTicketCodeAndPhoneWithDetails(ticketCode, phone)
                .map(ticket -> {
                    model.addAttribute("ticket", ticket);
                    return "ticket_lookup";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Không tìm thấy vé.");
                    return "ticket_lookup";
                });
    }

    // CORE-09: Xem danh sách vé
    @GetMapping("/passenger/my-tickets")
    public String myTickets(@RequestParam(defaultValue = "0") int page,
                            Model model,
                            Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Pageable pageable = PageRequest.of(page, 5);
        Page<Ticket> ticketPage = ticketRepository.findByUserIdWithDetailsPage(user.getId(), pageable);
        model.addAttribute("tickets", ticketPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ticketPage.getTotalPages());
        model.addAttribute("totalItems", ticketPage.getTotalElements());
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

        emailService.sendCancelNotification(ticket);

        redirectAttrs.addFlashAttribute("success", "Hủy vé thành công! Mã vé: " + ticket.getTicketCode());
        return "redirect:/passenger/my-tickets";
    }
}