package com.example.busticketpro.controller;

import com.example.busticketpro.dto.TripDTO;
import com.example.busticketpro.model.Seat;
import com.example.busticketpro.model.Trip;
import com.example.busticketpro.service.BusService;
import com.example.busticketpro.service.SeatService;
import com.example.busticketpro.service.TripService;
import com.example.busticketpro.repository.RouteRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/trips")
public class TripController {

    @Autowired private TripService tripService;
    @Autowired private BusService busService;
    @Autowired private RouteRepository routeRepository;
    @Autowired private SeatService seatService;

    // ── Danh sách chuyến cho admin
    @GetMapping
    public String listTrips(Model model, Authentication authentication) {
        List<Trip> trips = tripService.getAllTripsForAdmin();
        LocalDateTime now = LocalDateTime.now();

        // Map tripId → isDeparted để Thymeleaf kiểm tra từng chuyến
        Map<Long, Boolean> departedMap = new HashMap<>();
        for (Trip t : trips) {
            departedMap.put(t.getId(), t.getDepartureTime().isBefore(now));
        }

        model.addAttribute("trips", trips);
        model.addAttribute("departedMap", departedMap);
        model.addAttribute("username", authentication.getName());
        return "trips";
    }

    // ── Form thêm chuyến mới
    @GetMapping("/new")
    public String newTripForm(Model model, Authentication authentication) {
        model.addAttribute("tripDTO", new TripDTO());
        model.addAttribute("buses", busService.getAll());
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("username", authentication.getName());
        return "trip_form";
    }

    // ── Tạo chuyến mới
    @PostMapping("/create")
    public String createTrip(@ModelAttribute @Valid TripDTO dto,
                             BindingResult bindingResult,
                             Model model, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("buses", busService.getAll());
            model.addAttribute("routes", routeRepository.findAll());
            model.addAttribute("username", authentication.getName());
            return "trip_form";
        }
        try {
            tripService.createTrip(dto);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("buses", busService.getAll());
            model.addAttribute("routes", routeRepository.findAll());
            model.addAttribute("username", authentication.getName());
            return "trip_form";
        }
        return "redirect:/admin/trips";
    }

    // ── Xóa chuyến xe (Admin)
    @GetMapping("/delete/{id}")
    public String deleteTrip(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            tripService.deleteTrip(id);
            redirectAttrs.addFlashAttribute("successMsg", "Đã xóa chuyến xe thành công!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/trips";
    }

    // ── Xem sơ đồ ghế
    @GetMapping("/{tripId}/seats")
    public String viewSeats(@PathVariable Long tripId, Model model, Authentication authentication) {
        Trip trip = tripService.getById(tripId);
        List<Seat> seats = seatService.getSeatsForTrip(tripId);
        model.addAttribute("trip", trip);
        model.addAttribute("seats", seats);
        model.addAttribute("username", authentication.getName());
        return "seat_map";
    }
}