package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.ReservationRequest;
import com.example.bookingsystem.dto.ReservationResponse;
import com.example.bookingsystem.dto.ReservationUpdateRequest;
import com.example.bookingsystem.entity.ReservationStatus;
import com.example.bookingsystem.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<ReservationResponse> reservations = reservationService.getReservations(status, minPrice, maxPrice, pageable, authentication);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(reservationService.getReservationById(id, authentication));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request,
                                                                 Authentication authentication) {
        ReservationResponse created = reservationService.createReservation(request, authentication);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> updateReservation(@PathVariable Long id,
                                                                 @Valid @RequestBody ReservationUpdateRequest request) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    private Sort parseSort(String[] sort) {
        if (sort.length == 0) return Sort.unsorted();
        String property = sort[0];
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
