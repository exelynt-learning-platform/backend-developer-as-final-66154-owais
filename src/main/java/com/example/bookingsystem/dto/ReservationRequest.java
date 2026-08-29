package com.example.bookingsystem.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationRequest(
    @NotNull Long resourceId,
    @NotNull @FutureOrPresent LocalDateTime startTime,
    @NotNull @Future LocalDateTime endTime,
    @NotNull @DecimalMin("0.0") BigDecimal price
) {}
