package com.example.bookingsystem.repository;

import com.example.bookingsystem.entity.Reservation;
import com.example.bookingsystem.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // For USER: get own reservations (with optional filters)
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    Page<Reservation> findByUserIdAndPriceBetween(Long userId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Reservation> findByUserIdAndStatusAndPriceBetween(Long userId, ReservationStatus status,
                                                           BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // For ADMIN: get all reservations (with optional filters)
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    Page<Reservation> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Reservation> findByStatusAndPriceBetween(ReservationStatus status, BigDecimal minPrice,
                                                  BigDecimal maxPrice, Pageable pageable);
}
