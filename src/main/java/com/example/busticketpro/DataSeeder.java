package com.example.busticketpro;

import com.example.busticketpro.model.Role;
import com.example.busticketpro.model.User;
import com.example.busticketpro.model.UserProfile;
import com.example.busticketpro.repository.ProfileRepository;
import com.example.busticketpro.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository,
                                       ProfileRepository profileRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(encoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                User saved = userRepository.save(admin);

                //  Tạo Profile cho admin
                UserProfile profile = new UserProfile();
                profile.setUser(saved);
                profile.setFullName("Quản trị viên");
                profile.setPhone("0000000000");
                profile.setEmail("admin@busticketpro.com");
                profileRepository.save(profile);

                System.out.println(" Admin và Profile đã được tạo");
            }
        };
    }
}