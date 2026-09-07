package com.apigateway.auth.config;

import com.apigateway.auth.entity.User;
import com.apigateway.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            if (userRepository.count() == 0) {
                log.info("Seeding initial users into Auth database...");

                User admin = new User(
                        "admin",
                        "admin@gateway.io",
                        passwordEncoder.encode("admin123"),
                        "ROLE_ADMIN",
                        "ak_admin_prod_key_77889900112233"
                );

                User dev = new User(
                        "developer",
                        "dev@gateway.io",
                        passwordEncoder.encode("developer123"),
                        "ROLE_DEVELOPER",
                        "ak_dev_sandbox_key_44556677889900"
                );

                User demo = new User(
                        "demouser",
                        "demo@gateway.io",
                        passwordEncoder.encode("demo123"),
                        "ROLE_USER",
                        "ak_demo_client_key_11223344556677"
                );

                userRepository.save(admin);
                userRepository.save(dev);
                userRepository.save(demo);

                log.info("Seeded 3 default users (admin, developer, demouser) successfully.");
            }
        } catch (Exception e) {
            log.warn("Database initialization notice: {}", e.getMessage());
        }
    }
}
