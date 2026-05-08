package com.example.busticketpro.controller;

import com.example.busticketpro.model.Role;
import com.example.busticketpro.model.User;
import com.example.busticketpro.model.UserProfile;
import com.example.busticketpro.repository.ProfileRepository;
import com.example.busticketpro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired private UserRepository userRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String listUsers(Model model, Authentication auth) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("username", auth.getName());
        return "admin_users";
    }

    @PostMapping("/create")
    public String createUser(@RequestParam(required = false) String username,
                             @RequestParam(required = false) String password,
                             @RequestParam Role role,
                             Model model, Authentication auth) {

        boolean hasError = false;

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorUsername", "Tên đăng nhập không được để trống");
            hasError = true;
        } else if (username.trim().length() < 4) {
            model.addAttribute("errorUsername", "Tên đăng nhập phải từ 4 ký tự trở lên");
            hasError = true;
        } else if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("errorUsername", "Tên đăng nhập đã tồn tại");
            hasError = true;
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("errorPassword", "Mật khẩu không được để trống");
            hasError = true;
        } else if (password.trim().length() < 8) {
            model.addAttribute("errorPassword", "Mật khẩu phải ít nhất 8 ký tự");
            hasError = true;
        }

        if (hasError) {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("username", auth.getName());
            return "admin_users";
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        User saved = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(saved);
        profile.setFullName("");
        profile.setPhone("0000000000");
        profile.setEmail("default@email.com");
        profileRepository.save(profile);

        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttrs) {
        // Không cho xóa chính mình
        User current = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (current.getId().equals(id)) {
            redirectAttrs.addFlashAttribute("error", "Không thể xóa tài khoản đang đăng nhập!");
            return "redirect:/admin/users";
        }
        try {
            profileRepository.findByUserId(id).ifPresent(profileRepository::delete);
            userRepository.deleteById(id);
            redirectAttrs.addFlashAttribute("success", "Xóa tài khoản thành công!");
        } catch (Exception e) {
            // ✅ Bắt lỗi ràng buộc FK
            redirectAttrs.addFlashAttribute("error",
                    "Không thể xóa tài khoản này vì đang có dữ liệu liên quan (vé đặt, lịch sử...)!");
        }
        return "redirect:/admin/users";
    }
}