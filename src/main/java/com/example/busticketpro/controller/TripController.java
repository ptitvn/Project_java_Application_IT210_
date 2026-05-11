package com.example.busticketpro.controller;

import com.example.busticketpro.dto.TripDTO;
import com.example.busticketpro.model.Seat;
import com.example.busticketpro.model.Trip;
import com.example.busticketpro.repository.RouteRepository;
import com.example.busticketpro.service.BusService;
import com.example.busticketpro.service.SeatService;
import com.example.busticketpro.service.TripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

//    Hiển thị danh sách các chuyến xe kèm phân trang và kiểm tra chuyến nào đã khởi hành.
    @GetMapping
    public String listTrips(@RequestParam(defaultValue = "0") int page,
                            Model model,
                            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, 5);
        Page<Trip> tripPage = tripService.getPageForAdmin(pageable);

        LocalDateTime now = LocalDateTime.now();
        Map<Long, Boolean> departedMap = new HashMap<>();

        for (Trip t : tripPage.getContent()) {
            departedMap.put(t.getId(), t.getDepartureTime().isBefore(now));
        }

        model.addAttribute("trips", tripPage.getContent());
        model.addAttribute("departedMap", departedMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tripPage.getTotalPages());
        model.addAttribute("totalItems", tripPage.getTotalElements());
        model.addAttribute("username", authentication.getName());

        return "trips";
    }

//    Hiển thị form để tạo chuyến xe mới (cung cấp danh sách xe và tuyến đường để chọn).
    @GetMapping("/new")
    public String newTripForm(Model model, Authentication authentication) {
        model.addAttribute("tripDTO", new TripDTO());
        model.addAttribute("buses", busService.getAll());
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("username", authentication.getName());
        return "trip_form";
    }

    //  Xử lý lưu thông tin chuyến xe mới vào hệ thống sau khi kiểm tra tính hợp lệ.
    @PostMapping("/create")
    public String createTrip(@ModelAttribute @Valid TripDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             Authentication authentication) {

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

//    Xóa một chuyến xe khỏi hệ thống.
    @GetMapping("/delete/{id}")
    public String deleteTrip(@PathVariable Long id,
                             RedirectAttributes redirectAttrs) {
        try {
            tripService.deleteTrip(id);
            redirectAttrs.addFlashAttribute("successMsg", "Đã xóa chuyến xe thành công!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/trips";
    }

//    Hiển thị sơ đồ chi tiết các ghế ngồi của một chuyến xe cụ thể để Admin theo dõi
    @GetMapping("/{tripId}/seats")
    public String viewSeats(@PathVariable Long tripId,
                            Model model,
                            Authentication authentication) {

        Trip trip = tripService.getById(tripId);
        List<Seat> seats = seatService.getSeatsForTrip(tripId);

        model.addAttribute("trip", trip);
        model.addAttribute("seats", seats);
        model.addAttribute("username", authentication.getName());

        return "seat_map";
    }
}