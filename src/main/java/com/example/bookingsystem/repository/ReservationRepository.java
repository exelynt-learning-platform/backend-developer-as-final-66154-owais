package com.example.bookingsystem.repository;

import com.example.bookingsystem.entity.Reservation;
import com.example.bookingsystem.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // For USER: get own reservations (with optional filters)
    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> findByUserIdAndPriceBetween(Long userId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> findByUserIdAndStatusAndPriceBetween(Long userId, ReservationStatus status,
                                                           BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // For ADMIN: get all reservations (with optional filters)
    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> findByStatusAndPriceBetween(ReservationStatus status, BigDecimal minPrice,
                                                  BigDecimal maxPrice, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "resource"})
    @Override
    Page<Reservation> findAll(Pageable pageable);

    /**
     * True when a non-cancelled reservation for the same resource overlaps
     * [startTime, endTime). Pass excludeId to ignore a reservation being updated.
     */
    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.status <> com.example.bookingsystem.entity.ReservationStatus.CANCELLED
              AND r.startTime < :endTime
              AND r.endTime > :startTime
              AND (:excludeId IS NULL OR r.id <> :excludeId)
            """)
    boolean existsOverlapping(@Param("resourceId") Long resourceId,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime,
                              @Param("excludeId") Long excludeId);

    boolean existsByResourceIdAndStatusNot(Long resourceId, ReservationStatus status);
}
