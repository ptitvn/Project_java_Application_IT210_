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
//  Xử lý nghiệp vụ đăng ký người dùng mới
    public User register(String username, String rawPassword, Role role) {
        if (userRepo.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        User saved = userRepo.save(u);

        UserProfile profile = new UserProfile();
        profile.setUser(saved);
        profile.setFullName("");
        profile.setPhone("0000000000");
        profile.setEmail("default@email.com");
        profileRepo.save(profile);

        return saved;
    }
}