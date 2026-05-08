package com.example.busticketpro.service;

import com.example.busticketpro.model.Role;
import com.example.busticketpro.model.User;
import com.example.busticketpro.model.UserProfile;
import com.example.busticketpro.repository.ProfileRepository;
import com.example.busticketpro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProfileRepository profileRepo;

    public User register(String username, String rawPassword, Role role) {
        if (userRepo.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        User saved = userRepo.save(u);

        //  Tự động tạo Profile rỗng
        UserProfile profile = new UserProfile();
        profile.setUser(saved);
        profile.setFullName("");
        profile.setPhone("0000000000");
        profile.setEmail("default@email.com");
        profileRepo.save(profile);

        return saved;
    }

    public User login(String username, String rawPassword) {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (!passwordEncoder.matches(rawPassword, u.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }
        return u;
    }
}