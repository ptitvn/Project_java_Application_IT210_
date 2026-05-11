package com.example.busticketpro.controller;

import com.example.busticketpro.dto.ProfileDTO;
import com.example.busticketpro.model.User;
import com.example.busticketpro.model.UserProfile;
import com.example.busticketpro.repository.UserRepository;
import com.example.busticketpro.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;
//  Lấy thông tin hồ sơ (profile) của người dùng hiện tại và hiển thị lên trang cá nhân.
    @GetMapping
    public String getProfile(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        UserProfile profile = profileService.getProfile(user.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("profileDTO", toDTO(profile));
        model.addAttribute("username", authentication.getName());
        return "profile";
    }

//    Cập nhật thông tin hồ sơ mới từ form và điều hướng người dùng về trang chủ tương ứng với vai trò (Role).
    @PostMapping("/update")
    public String updateProfile(Authentication authentication,
                                @ModelAttribute @Valid ProfileDTO dto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            model.addAttribute("profile", profileService.getProfile(user.getId()));
            model.addAttribute("profileDTO", dto);
            return "profile";
        }
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        profileService.updateProfile(user.getId(), dto);

        // Redirect đúng trang theo role
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/buses";
        } else if (role.equals("ROLE_STAFF")) {
            return "redirect:/staff/tickets";
        } else {
            return "redirect:/";
        }
    }

//Hàm hỗ trợ chuyển đổi dữ liệu từ đối tượng Entity (UserProfile) sang DTO (ProfileDTO) để dùng cho Form.
    private ProfileDTO toDTO(UserProfile p) {
        ProfileDTO dto = new ProfileDTO();
        dto.setFullName(p.getFullName());
        dto.setPhone(p.getPhone());
        dto.setEmail(p.getEmail());
        return dto;
    }
}