package com.example.bookingsystem.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(
    @NotNull Long resourceId,
    @NotNull @FutureOrPresent LocalDateTime startTime,
    @NotNull @Future LocalDateTime endTime
) {}
