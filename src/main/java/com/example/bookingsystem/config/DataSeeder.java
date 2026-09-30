package com.example.bookingsystem.config;


import com.example.bookingsystem.entity.*;
import com.example.bookingsystem.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.seed.admin-email:admin@example.com}")
    private String adminEmail;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository,
                      ResourceRepository resourceRepository, ReservationRepository reservationRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Seed data disabled (app.seed.enabled=false), skipping DataSeeder");
            return;
        }
        log.warn("DataSeeder is active: demo users/resources will be created. Disable in production (app.seed.enabled=false).");

        // Create roles if not exist
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_USER)));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_ADMIN)));

        // Create users if not exist
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .email(adminEmail)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
        }
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .email("user@example.com")
                    .roles(Set.of(userRole))
                    .build();
            userRepository.save(user);
        }

        // Create sample resources if none exist
        if (resourceRepository.count() == 0) {
            Resource room = Resource.builder()
                    .name("Conference Room A")
                    .description("Large room with projector")
                    .available(true)
                    .pricePerHour(new BigDecimal("50.00"))
                    .build();
            Resource car = Resource.builder()
                    .name("Company Car")
                    .description("Sedan for business trips")
                    .available(true)
                    .pricePerHour(new BigDecimal("25.00"))
                    .build();
            resourceRepository.save(room);
            resourceRepository.save(car);

            // Create a sample reservation for user (2 hours x 50.00 = 100.00)
            User user = userRepository.findByUsername("user").orElseThrow();
            Reservation reservation = Reservation.builder()
                    .user(user)
                    .resource(room)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                    .price(new BigDecimal("100.00"))
                    .status(ReservationStatus.PENDING)
                    .build();
            reservationRepository.save(reservation);
        }
    }
}
