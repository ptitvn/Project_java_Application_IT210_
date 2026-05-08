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

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "register";
    }

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
