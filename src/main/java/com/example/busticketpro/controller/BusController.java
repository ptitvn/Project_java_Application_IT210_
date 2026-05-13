package com.example.busticketpro.controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/admin/buses")
public class BusController {

    @Autowired
    private BusService busService;
//  Hiển thị danh sách các xe khách (buses) kèm theo tính năng phân trang.
    @GetMapping
    public String listBuses(@RequestParam(defaultValue = "0") int page,
                            Model model,
                            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, 5);
        Page<Bus> busPage = busService.getPage(pageable);

        model.addAttribute("buses", busPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", busPage.getTotalPages());
        model.addAttribute("totalItems", busPage.getTotalElements());
        model.addAttribute("username", authentication.getName());

        return "buses";
    }
//  Hiển thị form để Admin nhập thông tin thêm xe khách mới.
    @GetMapping("/new")
    public String newBusForm(Model model) {
        model.addAttribute("busDTO", new BusDTO());
        return "bus_form";
    }

//  Xử lý lưu thông tin xe khách mới vào database sau khi đã kiểm tra lỗi nhập liệu.
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

//    Tìm thông tin xe theo ID và hiển thị form để chỉnh sửa thông tin xe đó.
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

// Cập nhật thông tin xe khách sau khi Admin thực hiện chỉnh sửa trong form.
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

//  Xóa một xe khách khỏi hệ thống dựa trên mã ID đã chọn.
@GetMapping("/delete/{id}")
public String deleteBus(@PathVariable Long id, RedirectAttributes redirectAttrs) {
    try {
        busService.deleteBus(id);
        redirectAttrs.addFlashAttribute("successMsg", "Xóa xe thành công!");
    } catch (RuntimeException e) {
        redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/admin/buses";
}
    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private RouteRepository routeRepository;

//    Hiển thị danh sách tất cả các địa điểm (tỉnh/thành phố) trong hệ thống.
    @GetMapping("/locations")
    public String listLocations(Model model, Authentication authentication) {
        model.addAttribute("locations", locationRepository.findAll());
        model.addAttribute("username", authentication.getName());
        return "locations";
    }
//  Hiển thị danh sách tất cả các tuyến đường (routes) đang hoạt động.
    @GetMapping("/routes")
    public String listRoutes(Model model, Authentication authentication) {
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("username", authentication.getName());
        return "routes";
    }
}