package com.example.bookingsystem.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ResourceRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description,
    boolean available,
    @NotNull @DecimalMin("0.0") BigDecimal pricePerHour
) {}
