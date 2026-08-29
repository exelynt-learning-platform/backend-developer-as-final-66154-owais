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

@Service
public class ReservationService {

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
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

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
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Long userId = user.getId();

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
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !reservation.getUser().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("You are not authorized to view this reservation");
        }
        return DtoMapper.toReservationResponse(reservation);
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.resourceId()));

        if (!resource.isAvailable()) {
            throw new IllegalStateException("Resource is not available");
        }
        if (request.endTime().isBefore(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .price(request.price())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return DtoMapper.toReservationResponse(saved);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (request.startTime() != null) reservation.setStartTime(request.startTime());
        if (request.endTime() != null) reservation.setEndTime(request.endTime());
        if (request.price() != null) reservation.setPrice(request.price());
        if (request.status() != null) reservation.setStatus(request.status());

        return DtoMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation not found with id: " + id);
        }
        reservationRepository.deleteById(id);
    }
}