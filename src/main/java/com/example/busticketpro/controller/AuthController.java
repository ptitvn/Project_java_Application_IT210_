package com.example.busticketpro.controller;

import com.example.busticketpro.dto.UserDTO;
import com.example.busticketpro.model.User;
import com.example.busticketpro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
@Controller
public class AuthController {

//  Điều hướng người dùng về trang chủ tương ứng dựa trên vai trò (Admin, Nhân viên, hoặc Khách).
    @Autowired
    private AuthService authService;
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/buses";
        } else if (role.equals("ROLE_STAFF")) {
            return "redirect:/staff/tickets";
        } else {
            model.addAttribute("username", authentication.getName());
            return "index";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
// Chuẩn bị dữ liệu mẫu (UserDTO) và hiển thị giao diện trang đăng ký tài khoản.
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "register";
    }

//  Xử lý logic đăng ký tài khoản mới, kiểm tra lỗi nhập liệu, khớp mật khẩu và lưu vào database.
    @PostMapping("/register")
    public String register(@ModelAttribute @Valid UserDTO dto,
                           BindingResult bindingResult,
                           @RequestParam String confirmPassword,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        if (!dto.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "register";
        }
        try {
            authService.register(dto.getUsername(), dto.getPassword(), dto.getRole());
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
