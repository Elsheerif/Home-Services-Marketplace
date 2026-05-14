package com.marketplace.userservice.config;

import com.marketplace.userservice.entity.User;
import com.marketplace.userservice.entity.UserRole;
import com.marketplace.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user on startup
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
            System.out.println("[DataInitializer] Admin user created: admin / admin123");
        }
    }
}
