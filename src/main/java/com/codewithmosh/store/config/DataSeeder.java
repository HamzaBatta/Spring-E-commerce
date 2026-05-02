package com.codewithmosh.store.config;

import com.codewithmosh.store.entities.Admin;
import com.codewithmosh.store.repositories.AdminRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!adminRepository.existsByEmail("admin@store.com")) {
            var admin = Admin.builder()
                    .name("Admin")
                    .email("admin@store.com")
                    .password(passwordEncoder.encode("admin123"))
                    .build();
            adminRepository.save(admin);
            System.out.println("Default admin created → email: admin@store.com | password: admin123");
        }
    }
}
