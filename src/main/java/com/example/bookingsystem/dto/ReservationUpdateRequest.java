package com.example.bookingsystem.dto;


import com.example.bookingsystem.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationUpdateRequest(
    LocalDateTime startTime,
    LocalDateTime endTime,
    BigDecimal price,
    ReservationStatus status
) {}
