package com.example.busticketpro.service;

import com.example.busticketpro.dto.ProfileDTO;
import com.example.busticketpro.model.User;
import com.example.busticketpro.model.UserProfile;
import com.example.busticketpro.repository.ProfileRepository;
import com.example.busticketpro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepo;

    @Autowired
    private UserRepository userRepo;

    public UserProfile getProfile(Long userId) {
        return profileRepo.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepo.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    newProfile.setFullName("");
                    newProfile.setPhone("0000000000");
                    newProfile.setEmail("default@email.com");
                    return profileRepo.save(newProfile);
                });
    }

    public UserProfile updateProfile(Long userId, ProfileDTO dto) {
        UserProfile profile = getProfile(userId);
        profile.setFullName(dto.getFullName());
        profile.setPhone(dto.getPhone());
        profile.setEmail(dto.getEmail());
        return profileRepo.save(profile);
    }
}