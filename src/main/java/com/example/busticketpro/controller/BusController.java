package com.example.busticketpro.controller;

import com.example.busticketpro.dto.BusDTO;
import com.example.busticketpro.model.Bus;
import com.example.busticketpro.service.BusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import java.util.List;
import com.example.busticketpro.repository.LocationRepository;
import com.example.busticketpro.repository.RouteRepository;
@Controller
@RequestMapping("/admin/buses")
public class BusController {

    @Autowired
    private BusService busService;

    @GetMapping
    public String listBuses(Model model, Authentication authentication) {
        List<Bus> buses = busService.getAll();
        model.addAttribute("buses", buses);
        model.addAttribute("username", authentication.getName());
        return "buses";
    }

    @GetMapping("/new")
    public String newBusForm(Model model) {
        model.addAttribute("busDTO", new BusDTO());
        return "bus_form";
    }

    @PostMapping("/create")
    public String createBus(@ModelAttribute @Valid BusDTO dto,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "bus_form";
        }
        try {
            busService.createBus(dto);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "bus_form";
        }
        return "redirect:/admin/buses";
    }

    @GetMapping("/edit/{id}")
    public String editBusForm(@PathVariable Long id, Model model) {
        Bus bus = busService.getAll().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        BusDTO dto = new BusDTO();
        dto.setLicensePlate(bus.getLicensePlate());
        dto.setType(bus.getType());
        dto.setSeatCount(bus.getSeatCount());
        dto.setDriverName(bus.getDriverName());
        model.addAttribute("busDTO", dto);
        model.addAttribute("busId", id);
        return "bus_form";
    }

    @PostMapping("/update/{id}")
    public String updateBus(@PathVariable Long id,
                            @ModelAttribute @Valid BusDTO dto,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("busId", id);
            return "bus_form";
        }
        busService.updateBus(id, dto);
        return "redirect:/admin/buses";
    }

    @GetMapping("/delete/{id}")
    public String deleteBus(@PathVariable Long id) {
        busService.deleteBus(id);
        return "redirect:/admin/buses";
    }

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private RouteRepository routeRepository;

    @GetMapping("/locations")
    public String listLocations(Model model, Authentication authentication) {
        model.addAttribute("locations", locationRepository.findAll());
        model.addAttribute("username", authentication.getName());
        return "locations";
    }

    @GetMapping("/routes")
    public String listRoutes(Model model, Authentication authentication) {
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("username", authentication.getName());
        return "routes";
    }
}