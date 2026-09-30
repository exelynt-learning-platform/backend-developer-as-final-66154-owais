package com.example.bookingsystem.service;

import com.example.bookingsystem.dto.DtoMapper;
import com.example.bookingsystem.dto.ReservationRequest;
import com.example.bookingsystem.dto.ReservationResponse;
import com.example.bookingsystem.dto.ReservationUpdateRequest;
import com.example.bookingsystem.entity.*;
import com.example.bookingsystem.exception.ResourceNotFoundException;
import com.example.bookingsystem.repository.ReservationRepository;
import com.example.bookingsystem.repository.ResourceRepository;
import com.example.bookingsystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class ReservationService {

    private static final Map<ReservationStatus, Set<ReservationStatus>> ALLOWED_TRANSITIONS = Map.of(
            ReservationStatus.PENDING, Set.of(ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED),
            ReservationStatus.CONFIRMED, Set.of(ReservationStatus.CANCELLED),
            ReservationStatus.CANCELLED, Set.of()
    );

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ResourceRepository resourceRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(ReservationStatus status,
                                                     BigDecimal minPrice,
                                                     BigDecimal maxPrice,
                                                     Pageable pageable,
                                                     Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);

        if (isAdmin) {
            // Admin: no user restriction
            if (status != null && minPrice != null && maxPrice != null) {
                return reservationRepository.findByStatusAndPriceBetween(status, minPrice, maxPrice, pageable)
                        .map(DtoMapper::toReservationResponse);
            } else if (status != null) {
                return reservationRepository.findByStatus(status, pageable)
                        .map(DtoMapper::toReservationResponse);
            } else if (minPrice != null && maxPrice != null) {
                return reservationRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                        .map(DtoMapper::toReservationResponse);
            } else {
                return reservationRepository.findAll(pageable)
                        .map(DtoMapper::toReservationResponse);
            }
        } else {
            // Regular user: only own reservations
            Long userId = getUser(authentication).getId();

            if (status != null && minPrice != null && maxPrice != null) {
                return reservationRepository.findByUserIdAndStatusAndPriceBetween(userId, status, minPrice, maxPrice, pageable)
                        .map(DtoMapper::toReservationResponse);
            } else if (status != null) {
                return reservationRepository.findByUserIdAndStatus(userId, status, pageable)
                        .map(DtoMapper::toReservationResponse);
            } else if (minPrice != null && maxPrice != null) {
                return reservationRepository.findByUserIdAndPriceBetween(userId, minPrice, maxPrice, pageable)
                        .map(DtoMapper::toReservationResponse);
            } else {
                return reservationRepository.findByUserId(userId, pageable)
                        .map(DtoMapper::toReservationResponse);
            }
        }
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, Authentication authentication) {
        Reservation reservation = findById(id);

        if (!isAdmin(authentication) && !reservation.getUser().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("You are not authorized to view this reservation");
        }
        return DtoMapper.toReservationResponse(reservation);
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, Authentication authentication) {
        User user = getUser(authentication);
        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.resourceId()));

        if (!resource.isAvailable()) {
            throw new IllegalStateException("Resource is not available");
        }
        validateTimeRange(request.startTime(), request.endTime());
        assertNoOverlap(resource.getId(), request.startTime(), request.endTime(), null);

        BigDecimal price = calculatePrice(resource.getPricePerHour(), request.startTime(), request.endTime());

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .price(price)
                .status(ReservationStatus.PENDING)
                .build();

        return DtoMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = findById(id);

        LocalDateTime newStart = request.startTime() != null ? request.startTime() : reservation.getStartTime();
        LocalDateTime newEnd = request.endTime() != null ? request.endTime() : reservation.getEndTime();

        if (request.startTime() != null || request.endTime() != null) {
            validateTimeRange(newStart, newEnd);
            assertNoOverlap(reservation.getResource().getId(), newStart, newEnd, reservation.getId());
            reservation.setStartTime(newStart);
            reservation.setEndTime(newEnd);
            // Re-price when the time window changes
            reservation.setPrice(calculatePrice(reservation.getResource().getPricePerHour(), newStart, newEnd));
        }

        if (request.status() != null && request.status() != reservation.getStatus()) {
            validateTransition(reservation.getStatus(), request.status());
            reservation.setStatus(request.status());
        }

        return DtoMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    /**
     * Soft-cancel: available to the owning user and to admins.
     */
    @Transactional
    public ReservationResponse cancelReservation(Long id, Authentication authentication) {
        Reservation reservation = findById(id);

        if (!isAdmin(authentication) && !reservation.getUser().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("You are not authorized to cancel this reservation");
        }
        validateTransition(reservation.getStatus(), ReservationStatus.CANCELLED);
        reservation.setStatus(ReservationStatus.CANCELLED);
        return DtoMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation not found with id: " + id);
        }
        reservationRepository.deleteById(id);
    }

    // ---- helpers ----

    private Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void assertNoOverlap(Long resourceId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (reservationRepository.existsOverlapping(resourceId, start, end, excludeId)) {
            throw new IllegalStateException("Resource is already booked for the requested time slot");
        }
    }

    private void validateTransition(ReservationStatus from, ReservationStatus to) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalStateException(
                    "Cannot change reservation status from " + from + " to " + to);
        }
    }

    static BigDecimal calculatePrice(BigDecimal pricePerHour, LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return pricePerHour.multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}
